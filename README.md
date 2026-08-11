# LyraShop Backend

Spring Boot REST API for the LyraShop e-commerce platform.

## Requirements

- Java 21
- Docker with a running Linux container engine

Maven is provided through the repository wrapper.

## Verify

On Windows:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

On Linux or macOS:

```sh
./mvnw --batch-mode --no-transfer-progress clean verify
```

The verification suite starts a disposable MySQL 8.0 container. Docker must be running.
CI also runs `bash .github/scripts/test-compose-deployment.sh` to build the
runtime image and verify the private network boundary end to end.

## Run locally

Set the required database environment variables before starting the application:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/lyrashop_db?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true"
$env:DB_USERNAME="lyrashop"
$env:DB_PASSWORD="replace-with-a-local-password"
$jwtKey = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtKey)
$env:JWT_SECRET_BASE64=[Convert]::ToBase64String($jwtKey)
.\mvnw.cmd spring-boot:run
```

The application listens on port `8080` by default. Set `SERVER_PORT` to override it.

## Authentication API

- `POST /api/v1/auth/register` creates an active `CUSTOMER` account.
- `POST /api/v1/auth/login` returns a short-lived HS256 access token for an
  active account.

Login responses contain only `accessToken`, `tokenType`, and `expiresIn`
and are marked `no-store`. Passwords are treated as opaque input and are never
trimmed or returned. Refresh-token issuance, rotation, and logout are separate
follow-up slices and are not implemented by this access-token endpoint.

## Run with Docker Compose

The Compose stack runs MySQL, the backend, and Nginx. Only Nginx publishes a
host port; the backend and database stay on isolated Docker networks.

Create local configuration and secret files:

```powershell
Copy-Item .env.example .env
New-Item -ItemType Directory -Force deploy/secrets | Out-Null
[guid]::NewGuid().ToString("N") |
    Set-Content -NoNewline deploy/secrets/mysql_password.txt
[guid]::NewGuid().ToString("N") |
    Set-Content -NoNewline deploy/secrets/mysql_root_password.txt
$jwtKey = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtKey)
[Convert]::ToBase64String($jwtKey) |
    Set-Content -NoNewline deploy/secrets/jwt_secret_base64.txt
```

Validate and start the stack:

```powershell
docker compose config --quiet
docker compose up --build --wait
curl.exe -H "Host: api.lyrashop.local" http://127.0.0.1:8080/nginx-health
```

The default edge binding is `127.0.0.1:8080`. The backend runs as UID/GID
`10001` with a read-only root filesystem, and its Actuator readiness endpoint
is bound only to `127.0.0.1:8081` inside the backend container. MySQL data is
stored in the `mysql_data` named volume. Keep both MySQL secret files stable
for the lifetime of that volume. Changing a file does not rotate credentials in
an initialized database; use `ALTER USER` or deliberately reset the local
volume. Remove the volume only when a full local database reset is intended:

```powershell
docker compose down
docker compose down --volumes
```

JWT access tokens use HS256 with a Base64-encoded key of at least 32 random
bytes. Compose mounts that key from `JWT_SECRET_BASE64_SECRET_FILE`; it is not
stored in the backend container environment. The default access-token lifetime
is 15 minutes and may only be configured between 15 and 30 minutes.

This Compose file closes direct host-port access to the backend and database,
but it is not the complete public production deployment. Keep the loopback
binding until TLS, firewall rules, DNS, backup, and monitoring are configured.

Authentication resource guards are configurable through:

- `AUTH_MAX_REQUEST_BODY_BYTES` (default `8192`)
- `AUTH_MAX_CONCURRENT_PASSWORD_HASHES` (default `2`)
- `AUTH_RETRY_AFTER_SECONDS` (default `1`)

The password-operation limit is local to each application instance and covers
both registration hashing and login verification. Production ingress must also
enforce shared authentication rate limits and request-size caps.

## Nginx authentication ingress

The production ingress template is
`deploy/nginx/templates/default.conf.template`. The official Nginx
container renders it through `envsubst`; set
`NGINX_ENVSUBST_FILTER=^(API_|AUTH_|BACKEND_|LOGIN_|REGISTRATION_)` so Nginx
runtime variables remain intact. Required deployment values are documented in
`.env.example`. `REGISTRATION_CORS_ALLOWED_ORIGIN` must also be present in
the backend `CORS_ALLOWED_ORIGINS` list. The shared
`AUTH_MAX_REQUEST_BODY_BYTES` value keeps the edge and application body caps
aligned for registration; `LOGIN_MAX_REQUEST_BODY_BYTES` may apply a stricter
edge cap to the smaller login payload.

The ingress applies independent per-IP and global quotas to registration and
login, caps both raw request bodies, overwrites client-supplied forwarding
headers, and returns stable problem responses for `429` and `413`.
Independent zones prevent a flood against one authentication flow from
exhausting the other. Rate-limit state is shared by all workers and backend
replicas behind one Nginx instance, but resets when that instance restarts and
is not shared across multiple Nginx instances.
The Docker DNS resolver refreshes recreated backend container addresses
without requiring an Nginx restart.

This control is active only when the backend port is private and all public API
traffic passes through Nginx. Do not publish backend port `8080` directly.
When another load balancer or CDN is added, configure Nginx real-IP handling
with explicit trusted CIDRs; never trust inbound forwarding headers globally.

Flyway migrations belong in `src/main/resources/db/migration`. Hibernate validates the migrated schema and never creates or updates it.
