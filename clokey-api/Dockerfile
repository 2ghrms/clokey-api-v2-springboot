FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :clokey-api:build -x test --no-daemon

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/clokey-api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
