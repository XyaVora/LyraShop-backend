FROM eclipse-temurin:25-jdk-alpine@sha256:3852a237086a660157560241b45ba32a547adcd93fadccaea108438bd523e053 AS build

WORKDIR /workspace

COPY gradle/ gradle/
COPY gradlew gradlew.bat settings.gradle build.gradle gradle.properties ./
RUN chmod +x gradlew

COPY src/ src/
RUN ./gradlew --no-daemon bootJar -x test \
    && test -f build/libs/app.jar \
    && cp build/libs/app.jar /workspace/app.jar

FROM eclipse-temurin:25-jre-alpine@sha256:cdd967aa55f1d0175ebe57245e4450292e6e6dd185dce73f93580598934128aa AS runtime

RUN addgroup -S -g 10001 lyrashop \
    && adduser -S -D -H -u 10001 -G lyrashop lyrashop

WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/app.jar /app/app.jar
RUN mkdir -p /app/uploads && chown 10001:10001 /app/uploads

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
