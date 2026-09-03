$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

function Fail([string]$Message) {
    Write-Error $Message
    exit 1
}

try {
    docker info --format "{{.ServerVersion}}" | Out-Null
} catch {
    Fail @"
Docker is not running. Local MySQL cannot start.

1. Start Docker Desktop and wait until it is ready.
2. Run this script again: .\scripts\local-up.ps1
"@
}

Write-Host "Starting local MySQL on 127.0.0.1:3307..."
docker compose -f compose.local.yaml up -d --wait
if ($LASTEXITCODE -ne 0) {
    Fail "docker compose -f compose.local.yaml up failed. Check Docker Desktop and port 3306."
}

$env:SPRING_PROFILES_ACTIVE = "dev"
$env:DB_PASSWORD = "lyrashop_local"

Write-Host "MySQL is up. Starting Spring Boot on http://127.0.0.1:8080 ..."
Write-Host "Health: http://127.0.0.1:8081/actuator/health"
Write-Host "Stop MySQL later with: .\scripts\local-down.ps1"

& .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
