# LyraShop Backend

Spring Boot REST API for the LyraShop e-commerce platform.

## Requirements

- Java 25
- Docker with a running Linux container engine

Gradle is provided through the repository wrapper.

## Verify

On Windows:

```powershell
.\gradlew.bat --no-daemon clean check
```

On Linux or macOS:

```sh
./gradlew --no-daemon clean check
```

The verification suite starts a disposable MySQL 8.0 container when Docker is
reachable. Locally, `check` skips those Testcontainers tests if Docker Desktop
is down. CI always runs them (`CI=true`). Force a skip with `-PskipDockerTests`.

CI also runs `bash .github/scripts/test-compose-deployment.sh` to build the
runtime image and verify the private network boundary end to end.

## Adding an endpoint

Keep new business routes under an existing prefix so Security and Nginx stay untouched:

1. Put ADMIN handlers under `/api/v1/admin/**` with `@PreAuthorize("hasRole('ADMIN')")`.
2. Put customer cart/order handlers under `/api/v1/cart/**` or `/api/v1/orders/**`.
   Profile stays on `/api/v1/me`.
3. Add a MockMvc test for anonymous `401`, wrong role `403`, and the happy path.
4. Only add a Security matcher or Nginx `location` when the path is a **new prefix** (not under `admin`, `cart`, `orders`, `me`, or `payments`).
5. Keep auth, health, and public catalog matchers explicit. Unknown prefixes stay deny-all.

Ambiguous paths (trailing slash, matrix parameters) are still rejected by the Nginx map.

## Run locally

This path is for daily development: MySQL in Docker, the API on the host.

1. Start Docker Desktop and wait until it is ready.
2. From the repo root:

```powershell
.\scripts\local-up.ps1
```

`.\run-local.ps1` is the same command. Do not point the API at a host MySQL on
3306 or set a per-boot JWT secret; the `dev` profile uses `127.0.0.1:3307`
and a stable local key.

On Linux or macOS:

```sh
chmod +x scripts/local-up.sh scripts/local-down.sh
./scripts/local-up.sh
```

The script starts MySQL on `127.0.0.1:3307` (container 3306), then `bootRun` with the `dev` profile. Host 3306 is often reserved on Windows.

- API: `http://127.0.0.1:8080`
- Health: `http://127.0.0.1:8081/actuator/health`

Stop MySQL with `.\scripts\local-down.ps1` (or `./scripts/local-down.sh`).

Register a customer (password at least 12 characters). PowerShell mangles inline JSON, so use a file:

```powershell
@'
{"email":"you@example.com","password":"LocalPass1234","fullName":"Local User"}
'@ | Set-Content -Encoding ascii register.json
curl.exe -sS -X POST http://127.0.0.1:8080/api/v1/auth/register `
  -H "Content-Type: application/json" `
  --data-binary "@register.json"
```

Login returns `accessToken`. Send it as `Authorization: Bearer ...` on cart, orders, and admin routes. Refresh cookies are `Secure`; use the access token for local HTTP calls.

Local admin (after the first `dev` boot): `admin@lyrashop.local` / `AdminPass1234`.

The `dev` profile is for this machine only. Production still uses `compose.yaml` with secret files and no published database port.

On first `dev` boot with no ADMIN row, the API creates `admin@lyrashop.local` /
`AdminPass1234` (override with `BOOTSTRAP_ADMIN_EMAIL` and
`BOOTSTRAP_ADMIN_PASSWORD`). Production leaves those empty unless you set the
same variables. An existing admin can also `PUT /api/v1/admin/users/{id}/role`
with `{"role":"ADMIN"}` or `{"role":"CUSTOMER"}`. The last remaining admin
cannot be demoted.

`lyrashop_schema.sql` is a readable snapshot of Flyway V1-V8 for Workbench.
Do not import it into the Docker MySQL used by `local-up`; the API applies
`src/main/resources/db/migration` on startup.

## Authentication API

- `POST /api/v1/auth/register` creates an active `CUSTOMER` account.
- `POST /api/v1/auth/login` returns a short-lived HS256 access token and starts
  a seven-day refresh-token family for an active account.
- `GET /api/v1/auth/csrf` bootstraps the CSRF header after a browser reload.
- `POST /api/v1/auth/refresh` rotates the refresh token and returns a new
  access token.
- `POST /api/v1/auth/logout` requires the bearer access token, revokes the
  current refresh-token family, and clears the authentication cookies.
- `GET /api/v1/me` returns the signed-in account. `PUT /api/v1/me` updates
  `fullName` and `phone`. Email cannot be changed.

COD orders stay unpaid until delivered. `VNPAY` is accepted when
`VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`, and `VNPAY_RETURN_URL` are set; the
create-order response then includes `paymentUrl`. VNPay calls
`GET /api/v1/payments/vnpay/ipn` and the browser returns to
`GET /api/v1/payments/vnpay/return`.

Login responses contain only `accessToken`, `tokenType`, and `expiresIn`
and are marked `no-store`. Passwords are treated as opaque input and are never
trimmed or returned. Raw refresh tokens are never returned in JSON or stored in
the database. They are carried only in the host-only
`__Secure-LyraShopRefresh` cookie with `HttpOnly`, `Secure`, `SameSite=Strict`,
and `Path=/api/v1/auth`.

Browser clients must use credentialed requests. Keep the `X-XSRF-TOKEN` value
from the login response header in memory and send it on refresh and logout.
After a page reload, call `GET /api/v1/auth/csrf` with credentials enabled to
obtain the header again before refreshing. The CSRF cookie is also host-only,
`HttpOnly`, `Secure`, and `SameSite=Strict`; frontend code does not read it.

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
Refresh-token families have a fixed seven-day lifetime by default, configured
with `REFRESH_TOKEN_TTL`. Rotation does not extend that absolute expiry.

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
`NGINX_ENVSUBST_FILTER=^(API_|AUTH_|BACKEND_|LOGIN_|LOGOUT_|REFRESH_|REGISTRATION_)`
so Nginx
runtime variables remain intact. Required deployment values are documented in
`.env.example`. `REGISTRATION_CORS_ALLOWED_ORIGIN` must also be present in
the backend `CORS_ALLOWED_ORIGINS` list. The shared
`AUTH_MAX_REQUEST_BODY_BYTES` value keeps the edge and application body caps
aligned for registration; `LOGIN_MAX_REQUEST_BODY_BYTES` may apply a stricter
edge cap to the smaller login payload.

The ingress applies independent per-IP and global quotas to registration,
login, refresh, and logout; caps all authentication request bodies; overwrites
client-supplied forwarding headers; and returns stable problem responses for
`429` and `413`. Refresh and logout bodies are capped at 1024 bytes. Edge error
responses preserve credentialed CORS semantics without logging authentication
cookies or CSRF headers.
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
