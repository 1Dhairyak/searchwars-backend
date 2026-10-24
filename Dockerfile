FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /src/target/higher-lower-game-1.0.0.jar app.jar
EXPOSE 8080
# Render free tier = 512MB RAM
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
