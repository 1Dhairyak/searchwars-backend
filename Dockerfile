FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY higher-lower-game-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
