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

## Run locally

Set the required database environment variables before starting the application:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/lyrashop_db?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true"
$env:DB_USERNAME="lyrashop"
$env:DB_PASSWORD="replace-with-a-local-password"
.\mvnw.cmd spring-boot:run
```

The application listens on port `8080` by default. Set `SERVER_PORT` to override it.

Registration resource guards are configurable through:

- `AUTH_MAX_REQUEST_BODY_BYTES` (default `8192`)
- `AUTH_MAX_CONCURRENT_PASSWORD_HASHES` (default `2`)
- `AUTH_RETRY_AFTER_SECONDS` (default `1`)

The password-hashing limit is local to each application instance. Production
ingress must also enforce a shared registration rate limit and request-size cap.

## Nginx registration ingress

The production ingress template is
`deploy/nginx/templates/default.conf.template`. The official Nginx
container renders it through `envsubst`; set
`NGINX_ENVSUBST_FILTER=^(API_|AUTH_|BACKEND_|REGISTRATION_)` so Nginx runtime
variables remain intact. Required deployment values are documented in
`.env.example`. `REGISTRATION_CORS_ALLOWED_ORIGIN` must also be present in
the backend `CORS_ALLOWED_ORIGINS` list. The shared
`AUTH_MAX_REQUEST_BODY_BYTES` value keeps the edge and application body caps
aligned.

The ingress applies both per-IP and global registration quotas, caps the raw
registration body, overwrites client-supplied forwarding headers, and returns
stable problem responses for `429` and `413`. Rate-limit state is shared by
all workers and backend replicas behind one Nginx instance, but resets when
that instance restarts and is not shared across multiple Nginx instances.
The Docker DNS resolver refreshes recreated backend container addresses
without requiring an Nginx restart.

This control is active only when the backend port is private and all public API
traffic passes through Nginx. Do not publish backend port `8080` directly.
When another load balancer or CDN is added, configure Nginx real-IP handling
with explicit trusted CIDRs; never trust inbound forwarding headers globally.

Flyway migrations belong in `src/main/resources/db/migration`. Hibernate validates the migrated schema and never creates or updates it.
