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
login_request_body="${secret_dir}/login-request.json"
invalid_login_request_body="${secret_dir}/invalid-login-request.json"
response_body="${secret_dir}/register-response.json"
error_body="${secret_dir}/error-response.json"
cors_headers="${secret_dir}/cors-headers.txt"
login_headers="${secret_dir}/login-headers.txt"
csrf_headers="${secret_dir}/csrf-headers.txt"
refresh_headers="${secret_dir}/refresh-headers.txt"
replay_headers="${secret_dir}/replay-headers.txt"
second_login_headers="${secret_dir}/second-login-headers.txt"
logout_headers="${secret_dir}/logout-headers.txt"
compose_logs="${secret_dir}/compose-logs.txt"

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
export LOGIN_MAX_REQUEST_BODY_BYTES=4096
export LOGIN_PER_IP_RATE=1r/m
export LOGIN_PER_IP_BURST=2
export LOGIN_GLOBAL_RATE=100r/m
export LOGIN_GLOBAL_BURST=100
export REFRESH_MAX_REQUEST_BODY_BYTES=1024
export REFRESH_PER_IP_RATE=30r/m
export REFRESH_PER_IP_BURST=10
export REFRESH_GLOBAL_RATE=300r/m
export REFRESH_GLOBAL_BURST=100
export LOGOUT_MAX_REQUEST_BODY_BYTES=1024
export LOGOUT_PER_IP_RATE=30r/m
export LOGOUT_PER_IP_BURST=10
export LOGOUT_GLOBAL_RATE=300r/m
export LOGOUT_GLOBAL_BURST=100

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
    rm -f "$app_secret" "$root_secret" "$jwt_secret" "$request_body" \
        "$login_request_body" "$invalid_login_request_body" \
        "$response_body" "$error_body" "$cors_headers" "$login_headers" \
        "$csrf_headers" "$refresh_headers" "$replay_headers" \
        "$second_login_headers" "$logout_headers" "$compose_logs" ||
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

header_value() {
    file=$1
    name=$2
    line="$(grep -i -m1 "^${name}:" "$file" || true)"
    [[ -n "$line" ]] || fail "response omitted $name header"
    printf '%s' "$line" | cut -d: -f2- | tr -d '\r' | sed 's/^[[:space:]]*//'
}

cookie_line() {
    file=$1
    name=$2
    line="$(grep -i "^set-cookie: ${name}=" "$file" | tail -n 1 || true)"
    [[ -n "$line" ]] || fail "response omitted $name cookie"
    printf '%s' "$line" | tr -d '\r'
}

cookie_value() {
    line="$(cookie_line "$1" "$2")"
    value="${line#*=}"
    printf '%s' "${value%%;*}"
}

assert_hardened_cookie() {
    file=$1
    name=$2
    line="$(cookie_line "$file" "$name")"
    normalized="$(printf '%s' "$line" | tr '[:upper:]' '[:lower:]')"
    [[ "$normalized" == *"path=/api/v1/auth"* ]] || fail "$name cookie path is not scoped"
    [[ "$normalized" =~ (^|\;[[:space:]]*)secure([[:space:]]*\;|$) ]] ||
        fail "$name cookie is not Secure"
    [[ "$normalized" =~ (^|\;[[:space:]]*)httponly([[:space:]]*\;|$) ]] ||
        fail "$name cookie is not HttpOnly"
    [[ "$normalized" == *"samesite=strict"* ]] || fail "$name cookie is not SameSite=Strict"
    [[ "$normalized" != *"domain="* ]] || fail "$name cookie must remain host-only"
}

sha256_upper() {
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum | awk '{print toupper($1)}'
    elif command -v shasum >/dev/null 2>&1; then
        shasum -a 256 | awk '{print toupper($1)}'
    elif command -v openssl >/dev/null 2>&1; then
        openssl dgst -sha256 | awk '{print toupper($NF)}'
    else
        fail "no SHA-256 command is available"
    fi
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

cors_status="$(curl -sS --connect-timeout 5 --max-time 15 -X OPTIONS -D "$cors_headers" -o /dev/null -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: content-type,x-xsrf-token' "${base_url}/api/v1/auth/refresh")"
[[ "$cors_status" == "200" ]] || fail "refresh CORS preflight returned HTTP $cors_status"
[[ "$(header_value "$cors_headers" "Access-Control-Allow-Origin")" == "https://shop.example.test" ]] ||
    fail "refresh CORS preflight omitted the allowed origin"
[[ "$(header_value "$cors_headers" "Access-Control-Allow-Credentials")" == "true" ]] ||
    fail "refresh CORS preflight did not allow credentials"
allowed_headers="$(header_value "$cors_headers" "Access-Control-Allow-Headers" | tr '[:upper:]' '[:lower:]')"
assert_contains "$allowed_headers" "x-xsrf-token" "refresh CORS preflight omitted CSRF header"

email="compose-${project}@example.test"
printf '{"email":"%s","password":"StrongPassword123!","fullName":"Compose Smoke"}' "$email" > "$request_body"
register_status="$(curl -sS --connect-timeout 5 --max-time 30 -o "$response_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Content-Type: application/json' --data-binary "@$request_body" "${base_url}/api/v1/auth/register")"
[[ "$register_status" == "201" ]] || fail "registration returned HTTP $register_status"
grep -Fq "$email" "$response_body" || fail "registration response omitted canonical email"

printf '{"email":"%s","password":"StrongPassword123!"}' "$email" > "$login_request_body"
login_status="$(curl -sS --connect-timeout 5 --max-time 30 -D "$login_headers" -o "$response_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H 'Content-Type: application/json' --data-binary "@$login_request_body" "${base_url}/api/v1/auth/login")"
[[ "$login_status" == "200" ]] || fail "login returned HTTP $login_status"
grep -Fq '"accessToken":"' "$response_body" || fail "login response omitted access token"
grep -Fq '"tokenType":"Bearer"' "$response_body" || fail "login response omitted bearer token type"
grep -Fq '"expiresIn":900' "$response_body" || fail "login response omitted access token lifetime"
[[ "$(header_value "$login_headers" "Access-Control-Allow-Credentials")" == "true" ]] ||
    fail "login response did not allow credentialed CORS"
first_access_token="$(grep -o '"accessToken":"[^"]*"' "$response_body" | head -n 1 | cut -d'"' -f4)"
[[ -n "$first_access_token" ]] || fail "login access token could not be parsed"
first_refresh_token="$(cookie_value "$login_headers" "__Secure-LyraShopRefresh")"
csrf_cookie="$(cookie_value "$login_headers" "__Secure-LyraShopCsrf")"
csrf_header="$(header_value "$login_headers" "X-XSRF-TOKEN")"
[[ "$first_refresh_token" =~ ^[A-Za-z0-9_-]{43}$ ]] || fail "refresh token format is invalid"
[[ -n "$csrf_cookie" && -n "$csrf_header" ]] || fail "login omitted CSRF material"
assert_hardened_cookie "$login_headers" "__Secure-LyraShopRefresh"
assert_hardened_cookie "$login_headers" "__Secure-LyraShopCsrf"
grep -Fq "$first_refresh_token" "$response_body" && fail "raw refresh token leaked in login JSON"
[[ "$backend_environment" != *"$first_refresh_token"* ]] || fail "raw refresh token leaked in backend environment"

csrf_status="$(curl -sS --connect-timeout 5 --max-time 15 -D "$csrf_headers" -o /dev/null -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H "Cookie: __Secure-LyraShopCsrf=${csrf_cookie}; __Secure-LyraShopRefresh=${first_refresh_token}" "${base_url}/api/v1/auth/csrf")"
[[ "$csrf_status" == "204" ]] || fail "CSRF bootstrap returned HTTP $csrf_status"
reloaded_csrf_header="$(header_value "$csrf_headers" "X-XSRF-TOKEN")"
[[ "$reloaded_csrf_header" == "$csrf_header" ]] || fail "CSRF bootstrap did not restore the session token"
[[ "$(header_value "$csrf_headers" "Access-Control-Allow-Credentials")" == "true" ]] ||
    fail "CSRF bootstrap did not allow credentialed CORS"
csrf_header="$reloaded_csrf_header"

first_refresh_digest="$(printf '%s' "$first_refresh_token" | sha256_upper)"
stored_initial_digest="$("${compose[@]}" exec -T mysql sh -ec 'export MYSQL_PWD="$(cat /run/secrets/mysql_password)"; mysql -u "$MYSQL_USER" "$MYSQL_DATABASE" --batch --skip-column-names --execute="SELECT HEX(token_hash) FROM refresh_sessions;"')"
[[ "$stored_initial_digest" == "$first_refresh_digest" ]] || fail "database did not persist only the refresh-token digest"

refresh_status="$(curl -sS --connect-timeout 5 --max-time 30 -X POST -D "$refresh_headers" -o "$response_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H "Cookie: __Secure-LyraShopRefresh=${first_refresh_token}; __Secure-LyraShopCsrf=${csrf_cookie}" -H "X-XSRF-TOKEN: ${csrf_header}" "${base_url}/api/v1/auth/refresh")"
[[ "$refresh_status" == "200" ]] || fail "refresh rotation returned HTTP $refresh_status"
grep -Fq '"accessToken":"' "$response_body" || fail "refresh response omitted access token"
rotated_refresh_token="$(cookie_value "$refresh_headers" "__Secure-LyraShopRefresh")"
[[ "$rotated_refresh_token" =~ ^[A-Za-z0-9_-]{43}$ ]] || fail "rotated refresh token format is invalid"
[[ "$rotated_refresh_token" != "$first_refresh_token" ]] || fail "refresh rotation reused the raw token"
assert_hardened_cookie "$refresh_headers" "__Secure-LyraShopRefresh"
grep -Fq "$first_refresh_token" "$response_body" && fail "old refresh token leaked in refresh JSON"
grep -Fq "$rotated_refresh_token" "$response_body" && fail "rotated refresh token leaked in refresh JSON"
csrf_header="$(header_value "$refresh_headers" "X-XSRF-TOKEN")"

rotated_refresh_digest="$(printf '%s' "$rotated_refresh_token" | sha256_upper)"
stored_rotation_digests="$("${compose[@]}" exec -T mysql sh -ec 'export MYSQL_PWD="$(cat /run/secrets/mysql_password)"; mysql -u "$MYSQL_USER" "$MYSQL_DATABASE" --batch --skip-column-names --execute="SELECT HEX(token_hash) FROM refresh_sessions ORDER BY created_at;"')"
grep -Fqx "$first_refresh_digest" <<<"$stored_rotation_digests" || fail "database lost the consumed refresh digest"
grep -Fqx "$rotated_refresh_digest" <<<"$stored_rotation_digests" || fail "database omitted the rotated refresh digest"

replay_status="$(curl -sS --connect-timeout 5 --max-time 30 -X POST -D "$replay_headers" -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H "Cookie: __Secure-LyraShopRefresh=${first_refresh_token}; __Secure-LyraShopCsrf=${csrf_cookie}" -H "X-XSRF-TOKEN: ${csrf_header}" "${base_url}/api/v1/auth/refresh")"
[[ "$replay_status" == "401" ]] || fail "refresh replay returned HTTP $replay_status"
grep -Fq '"code":"INVALID_REFRESH_TOKEN"' "$error_body" ||
    fail "refresh replay response omitted generic error code"
grep -Fq "$first_refresh_token" "$error_body" && fail "replayed token leaked in error JSON"

revoked_successor_status="$(curl -sS --connect-timeout 5 --max-time 30 -X POST -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H "Cookie: __Secure-LyraShopRefresh=${rotated_refresh_token}; __Secure-LyraShopCsrf=${csrf_cookie}" -H "X-XSRF-TOKEN: ${csrf_header}" "${base_url}/api/v1/auth/refresh")"
[[ "$revoked_successor_status" == "401" ]] ||
    fail "replay did not revoke the refresh family: HTTP $revoked_successor_status"
active_replayed_family="$("${compose[@]}" exec -T mysql sh -ec 'export MYSQL_PWD="$(cat /run/secrets/mysql_password)"; mysql -u "$MYSQL_USER" "$MYSQL_DATABASE" --batch --skip-column-names --execute="SELECT COUNT(*) FROM refresh_sessions WHERE revoked_at IS NULL;"')"
[[ "$active_replayed_family" == "0" ]] || fail "replayed refresh family remained active"

printf '{"email":"%s","password":"WrongPassword123!"}' "$email" > "$invalid_login_request_body"
invalid_login_status="$(curl -sS --connect-timeout 5 --max-time 30 -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Content-Type: application/json' --data-binary "@$invalid_login_request_body" "${base_url}/api/v1/auth/login")"
[[ "$invalid_login_status" == "401" ]] || fail "invalid login returned HTTP $invalid_login_status"
grep -Fq '"code":"INVALID_CREDENTIALS"' "$error_body" ||
    fail "invalid login response omitted generic error code"

second_login_status="$(curl -sS --connect-timeout 5 --max-time 30 -D "$second_login_headers" -o "$response_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H 'Content-Type: application/json' --data-binary "@$login_request_body" "${base_url}/api/v1/auth/login")"
[[ "$second_login_status" == "200" ]] || fail "second login returned HTTP $second_login_status"
logout_access_token="$(grep -o '"accessToken":"[^"]*"' "$response_body" | head -n 1 | cut -d'"' -f4)"
logout_refresh_token="$(cookie_value "$second_login_headers" "__Secure-LyraShopRefresh")"
logout_csrf_cookie="$(cookie_value "$second_login_headers" "__Secure-LyraShopCsrf")"
logout_csrf_header="$(header_value "$second_login_headers" "X-XSRF-TOKEN")"
[[ -n "$logout_access_token" && "$logout_refresh_token" =~ ^[A-Za-z0-9_-]{43}$ ]] ||
    fail "second login omitted authentication material"
assert_hardened_cookie "$second_login_headers" "__Secure-LyraShopRefresh"
assert_hardened_cookie "$second_login_headers" "__Secure-LyraShopCsrf"
grep -Fq "$logout_refresh_token" "$response_body" && fail "logout refresh token leaked in login JSON"

logout_status="$(curl -sS --connect-timeout 5 --max-time 30 -X POST -D "$logout_headers" -o /dev/null -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H "Authorization: Bearer ${logout_access_token}" -H "Cookie: __Secure-LyraShopRefresh=${logout_refresh_token}; __Secure-LyraShopCsrf=${logout_csrf_cookie}" -H "X-XSRF-TOKEN: ${logout_csrf_header}" "${base_url}/api/v1/auth/logout")"
[[ "$logout_status" == "204" ]] || fail "logout returned HTTP $logout_status"
assert_hardened_cookie "$logout_headers" "__Secure-LyraShopRefresh"
assert_hardened_cookie "$logout_headers" "__Secure-LyraShopCsrf"
refresh_clear_cookie="$(cookie_line "$logout_headers" "__Secure-LyraShopRefresh" | tr '[:upper:]' '[:lower:]')"
csrf_clear_cookie="$(cookie_line "$logout_headers" "__Secure-LyraShopCsrf" | tr '[:upper:]' '[:lower:]')"
[[ "$refresh_clear_cookie" == *"max-age=0"* ]] || fail "logout did not expire refresh cookie"
[[ "$csrf_clear_cookie" == *"max-age=0"* ]] || fail "logout did not expire CSRF cookie"

logged_out_refresh_status="$(curl -sS --connect-timeout 5 --max-time 30 -X POST -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H "Cookie: __Secure-LyraShopRefresh=${logout_refresh_token}; __Secure-LyraShopCsrf=${logout_csrf_cookie}" -H "X-XSRF-TOKEN: ${logout_csrf_header}" "${base_url}/api/v1/auth/refresh")"
[[ "$logged_out_refresh_status" == "401" ]] ||
    fail "logged-out refresh token returned HTTP $logged_out_refresh_status"
active_session_count="$("${compose[@]}" exec -T mysql sh -ec 'export MYSQL_PWD="$(cat /run/secrets/mysql_password)"; mysql -u "$MYSQL_USER" "$MYSQL_DATABASE" --batch --skip-column-names --execute="SELECT COUNT(*) FROM refresh_sessions WHERE revoked_at IS NULL;"')"
[[ "$active_session_count" == "0" ]] || fail "logout left an active refresh session"

"${compose[@]}" logs --no-color > "$compose_logs"
for raw_refresh_token in "$first_refresh_token" "$rotated_refresh_token" "$logout_refresh_token"; do
    [[ "$backend_environment" != *"$raw_refresh_token"* ]] ||
        fail "raw refresh token leaked in backend environment"
    if grep -F "$raw_refresh_token" "$compose_logs" >/dev/null; then
        fail "raw refresh token leaked in container logs"
    fi
done

limited_login_status="$(curl -sS --connect-timeout 5 --max-time 15 -D "$cors_headers" -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H 'Content-Type: application/json' --data-binary "@$login_request_body" "${base_url}/api/v1/auth/login")"
[[ "$limited_login_status" == "429" ]] ||
    fail "login ingress limiter returned HTTP $limited_login_status"
grep -Fq '"code":"LOGIN_RATE_LIMITED"' "$error_body" ||
    fail "login ingress limiter response omitted stable error code"
[[ "$(header_value "$cors_headers" "Access-Control-Allow-Credentials")" == "true" ]] ||
    fail "edge login limiter omitted credentialed CORS"
exposed_headers="$(header_value "$cors_headers" "Access-Control-Expose-Headers" | tr '[:upper:]' '[:lower:]')"
assert_contains "$exposed_headers" "x-xsrf-token" "edge login limiter omitted exposed CSRF header"

duplicate_status="$(curl -sS --connect-timeout 5 --max-time 30 -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Content-Type: application/json' --data-binary "@$request_body" "${base_url}/api/v1/auth/register")"
[[ "$duplicate_status" == "409" ]] || fail "duplicate registration returned HTTP $duplicate_status"

limited_status="$(curl -sS --connect-timeout 5 --max-time 15 -D "$cors_headers" -o "$error_body" -w '%{http_code}' -H "Host: ${API_SERVER_NAME}" -H 'Origin: https://shop.example.test' -H 'Content-Type: application/json' --data-binary "@$request_body" "${base_url}/api/v1/auth/register")"
[[ "$limited_status" == "429" ]] || fail "ingress limiter returned HTTP $limited_status"
[[ "$(header_value "$cors_headers" "Access-Control-Allow-Credentials")" == "true" ]] ||
    fail "edge registration limiter omitted credentialed CORS"

user_count="$("${compose[@]}" exec -T mysql sh -ec 'export MYSQL_PWD="$(cat /run/secrets/mysql_password)"; mysql -u "$MYSQL_USER" "$MYSQL_DATABASE" --batch --skip-column-names --execute="SELECT COUNT(*) FROM users;"')"
[[ "$user_count" == "1" ]] || fail "expected one persisted user, found $user_count"

printf 'Docker deployment smoke passed\n'
