FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :clokey-api:build -x test --no-daemon

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
ENV TZ=Asia/Seoul
COPY --from=builder /app/clokey-api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
