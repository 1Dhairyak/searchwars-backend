# ── Stage 1: Build ──────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
# Download dependencies first (layer cache)
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Railway injects $PORT — Spring reads server.port=${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
