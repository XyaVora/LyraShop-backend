#!/usr/bin/env bash

set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

project="lyrashop-smoke-${GITHUB_RUN_ID:-local}-${GITHUB_RUN_ATTEMPT:-1}-$$"
project="$(printf '%s' "$project" | tr '[:upper:]_' '[:lower:]-' | tr -cd 'a-z0-9-')"
secret_dir="target/${project}-secrets"
app_secret="${secret_dir}/mysql_password.txt"
root_secret="${secret_dir}/mysql_root_password.txt"
jwt_secret="${secret_dir}/jwt_secret_base64.txt"
request_body="${secret_dir}/register-request.json"
response_body="${secret_dir}/register-response.json"
error_body="${secret_dir}/error-response.json"

export MYSQL_USER=lyrashop
export MYSQL_PASSWORD_SECRET_FILE="./$app_secret"
export MYSQL_ROOT_PASSWORD_SECRET_FILE="./$root_secret"
export JWT_SECRET_BASE64_SECRET_FILE="./$jwt_secret"
export EDGE_BIND_ADDRESS=127.0.0.1
export EDGE_PORT="${COMPOSE_SMOKE_EDGE_PORT:-$((20000 + $$ % 30000))}"
export API_SERVER_NAME=api.lyrashop.test
export CORS_ALLOWED_ORIGINS=https://shop.example.test
export REGISTRATION_CORS_ALLOWED_ORIGIN=https://shop.example.test
export REGISTRATION_PER_IP_RATE=1r/m
export REGISTRATION_PER_IP_BURST=1
export REGISTRATION_GLOBAL_RATE=100r/m
export REGISTRATION_GLOBAL_BURST=100

compose=(docker compose -p "$project")

cleanup() {
    exit_code=$?
    cleanup_failed=0
    set +e
    if (( exit_code != 0 )); then
        "${compose[@]}" ps
        "${compose[@]}" logs --no-color
    fi
    "${compose[@]}" down --volumes --remove-orphans || cleanup_failed=1
    docker image rm "${project}-backend:latest" >/dev/null 2>&1 || true
    rm -f "$app_secret" "$root_secret" "$jwt_secret" "$request_body" "$response_body" "$error_body" ||
        cleanup_failed=1
    rmdir "$secret_dir" 2>/dev/null || cleanup_failed=1
    trap - EXIT
    if (( exit_code == 0 && cleanup_failed != 0 )); then
        printf 'Deployment smoke cleanup failed\n' >&2
        exit 1
    fi
    exit "$exit_code"
}
trap cleanup EXIT

umask 077
mkdir -p "$secret_dir"
chmod 700 "$secret_dir"
printf '%s' "smoke-app-${project}-7f14" > "$app_secret"
printf '%s' "smoke-root-${project}-c82a" > "$root_secret"
printf '%s' "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=" > "$jwt_secret"
chmod 0444 "$app_secret" "$root_secret" "$jwt_secret"

fail() {
    printf 'Deployment smoke failed: %s\n' "$*" >&2
    exit 1
}

assert_contains() {
    value=$1
    expected=$2
    message=$3
    [[ "$value" == *"$expected"* ]] || fail "$message: $value"
}

"${compose[@]}" config --quiet
"${compose[@]}" up -d --build --wait --wait-timeout 300

backend_id="$("${compose[@]}" ps -q backend)"
mysql_id="$("${compose[@]}" ps -q mysql)"
nginx_id="$("${compose[@]}" ps -q nginx)"
[[ -n "$backend_id" && -n "$mysql_id" && -n "$nginx_id" ]] ||
    fail "all service containers must exist"

backend_user="$(docker inspect --format '{{.Config.User}}' "$backend_id")"
backend_readonly="$(docker inspect --format '{{.HostConfig.ReadonlyRootfs}}' "$backend_id")"
backend_ports="$(docker inspect --format '{{json .HostConfig.PortBindings}}' "$backend_id")"
mysql_ports="$(docker inspect --format '{{json .HostConfig.PortBindings}}' "$mysql_id")"
nginx_user="$(docker inspect --format '{{.Config.User}}' "$nginx_id")"
nginx_readonly="$(docker inspect --format '{{.HostConfig.ReadonlyRootfs}}' "$nginx_id")"
nginx_ports="$(docker inspect --format '{{json .HostConfig.PortBindings}}' "$nginx_id")"
nginx_port_key_count="$(docker inspect --format '{{len .HostConfig.PortBindings}}' "$nginx_id")"
nginx_binding_count="$(docker inspect --format '{{len (index .HostConfig.PortBindings "8080/tcp")}}' "$nginx_id")"
nginx_host_ip="$(docker inspect --format '{{(index (index .HostConfig.PortBindings "8080/tcp") 0).HostIp}}' "$nginx_id")"
nginx_host_port="$(docker inspect --format '{{(index (index .HostConfig.PortBindings "8080/tcp") 0).HostPort}}' "$nginx_id")"

[[ "$backend_user" == "10001:10001" ]] || fail "backend must run as UID/GID 10001"
[[ "$backend_readonly" == "true" ]] || fail "backend root filesystem must be read-only"
[[ "$backend_ports" == "{}" ]] || fail "backend must not publish host ports"
[[ "$mysql_ports" == "{}" ]] || fail "mysql must not publish host ports"
[[ "$nginx_user" == "101:101" ]] || fail "nginx must run as its unprivileged user"
[[ "$nginx_readonly" == "true" ]] || fail "nginx root filesystem must be read-only"
assert_contains "$nginx_ports" '"8080/tcp"' "nginx must publish port 8080"
[[ "$nginx_port_key_count" == "1" && "$nginx_binding_count" == "1" ]] ||
    fail "nginx must have exactly one published binding, found $nginx_ports"
[[ "$nginx_host_ip" == "$EDGE_BIND_ADDRESS" ]] ||
    fail "nginx must bind only to $EDGE_BIND_ADDRESS, found $nginx_host_ip"
[[ "$nginx_host_port" == "$EDGE_PORT" ]] ||
    fail "nginx published port must be $EDGE_PORT, found $nginx_host_port"

backend_networks="$(docker inspect --format '{{range $name, $network := .NetworkSettings.Networks}}{{$name}}{{println}}{{end}}' "$backend_id" | sort | xargs)"
mysql_networks="$(docker inspect --format '{{range $name, $network := .NetworkSettings.Networks}}{{$name}}{{println}}{{end}}' "$mysql_id" | sort | xargs)"
nginx_networks="$(docker inspect --format '{{range $name, $network := .NetworkSettings.Networks}}{{$name}}{{println}}{{end}}' "$nginx_id" | sort | xargs)"

[[ "$backend_networks" == "${project}_data ${project}_edge" ]] ||
    fail "backend must join only the data and edge networks, found $backend_networks"
[[ "$mysql_networks" == "${project}_data" ]] ||
    fail "mysql must join only the data network, found $mysql_networks"
[[ "$nginx_networks" == "${project}_edge" ]] ||
    fail "nginx must join only the edge network, found $nginx_networks"

data_internal="$(docker network inspect "${project}_data" --format '{{.Internal}}')"
edge_internal="$(docker network inspect "${project}_edge" --format '{{.Internal}}')"
[[ "$data_internal" == "true" ]] || fail "data network must be internal"
[[ "$edge_internal" == "false" ]] || fail "edge network must allow the published ingress port"

backend_environment="$(docker inspect --format '{{range .Config.Env}}{{println .}}{{end}}' "$backend_id")"
if grep -q '^DB_PASSWORD=' <<<"$backend_environment"; then
    fail "database password must not be stored in backend environment"
fi
if grep -q '^JWT_SECRET_BASE64=' <<<"$backend_environment"; then
    fail "JWT signing secret must not be stored in backend environment"
fi

if "${compose[@]}" exec -T nginx wget -q -T 3 -O /dev/null http://backend:8081/actuator/health/readiness 2>/dev/null; then
    fail "backend management port must not be reachable from the edge network"
fi

base_url="http://127.0.0.1:${EDGE_PORT}"
health_status="$(curl -sS --connect-timeout 5 --max-time 15 -o /dev/null -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" "${base_url}/nginx-health")"
[[ "$health_status" == "204" ]] || fail "Nginx health returned HTTP $health_status"

actuator_status="$(curl -sS --connect-timeout 5 --max-time 15 -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" "${base_url}/actuator/health/readiness")"
[[ "$actuator_status" == "404" ]] || fail "public actuator route returned HTTP $actuator_status"

email="compose-${project}@example.test"
printf '{"email":"%s","password":"StrongPassword123!","fullName":"Compose Smoke"}' "$email" > "$request_body"
register_status="$(curl -sS --connect-timeout 5 --max-time 30 -o "$response_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Content-Type: application/json' --data-binary "@$request_body" "${base_url}/api/v1/auth/register")"
[[ "$register_status" == "201" ]] || fail "registration returned HTTP $register_status"
grep -Fq "$email" "$response_body" || fail "registration response omitted canonical email"

duplicate_status="$(curl -sS --connect-timeout 5 --max-time 30 -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Content-Type: application/json' --data-binary "@$request_body" "${base_url}/api/v1/auth/register")"
[[ "$duplicate_status" == "409" ]] || fail "duplicate registration returned HTTP $duplicate_status"

limited_status="$(curl -sS --connect-timeout 5 --max-time 15 -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Content-Type: application/json' --data-binary "@$request_body" "${base_url}/api/v1/auth/register")"
[[ "$limited_status" == "429" ]] || fail "ingress limiter returned HTTP $limited_status"

user_count="$("${compose[@]}" exec -T mysql sh -ec 'export MYSQL_PWD="$(cat /run/secrets/mysql_password)"; mysql -u "$MYSQL_USER" "$MYSQL_DATABASE" --batch --skip-column-names --execute="SELECT COUNT(*) FROM users;"')"
[[ "$user_count" == "1" ]] || fail "expected one persisted user, found $user_count"

printf 'Docker deployment smoke passed\n'
