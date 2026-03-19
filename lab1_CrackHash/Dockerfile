# syntax=docker/dockerfile:1.4
# Один этап Gradle для обоих JAR + кэш ~/.gradle (BuildKit) — без повторной загрузки дистрибутива
FROM eclipse-temurin:17-jdk AS gradle-build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :crackhash-manager:bootJar :crackhash-worker:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre AS manager
WORKDIR /app
COPY --from=gradle-build /app/crackhash-manager/build/libs/crackhash-manager-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS worker
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=gradle-build /app/crackhash-worker/build/libs/crackhash-worker-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
