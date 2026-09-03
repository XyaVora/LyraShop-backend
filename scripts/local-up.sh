#!/usr/bin/env sh
set -eu

repo_root="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
cd "$repo_root"

if ! docker info >/dev/null 2>&1; then
    printf '%s\n' "Docker is not running. Start Docker Desktop (or the engine) and retry." >&2
    exit 1
fi

echo "Starting local MySQL on 127.0.0.1:3307..."
docker compose -f compose.local.yaml up -d --wait

export SPRING_PROFILES_ACTIVE=dev
export DB_PASSWORD=lyrashop_local

echo "MySQL is up. Starting Spring Boot on http://127.0.0.1:8080 ..."
echo "Health: http://127.0.0.1:8081/actuator/health"
echo "Stop MySQL later with: ./scripts/local-down.sh"

exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
