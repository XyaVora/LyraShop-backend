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

Flyway migrations belong in `src/main/resources/db/migration`. Hibernate validates the migrated schema and never creates or updates it.
