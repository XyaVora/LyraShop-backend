# LyraShop Backend

Spring Boot REST API for the LyraShop e-commerce platform.

## Requirements

- Java 21

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

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

The application listens on port `8080` by default. Set `SERVER_PORT` to override it.
