FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /src/target/higher-lower-game-1.0.0.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination . && rm app.jar

# Render's free plan gives a fraction of a CPU, so a cold boot is mostly class loading.
# Class Data Sharing: start the app once at build time (no database needed: Flyway off,
# no JDBC metadata) and archive the loaded classes; real starts map the archive instead.
ENV JAVA_FLAGS="-XX:TieredStopAtLevel=1 -XX:+UseSerialGC"
RUN DB_URL=jdbc:postgresql://localhost:5999/none DB_USERNAME=x DB_PASSWORD=x \
    JWT_SECRET=cds-training-only-secret-not-used-at-runtime GOOGLE_CLIENT_ID=x \
    java $JAVA_FLAGS -XX:ArchiveClassesAtExit=app.jsa -Xlog:cds=off -Dspring.context.exit=onRefresh \
         -Dspring.profiles.active=prod -jar higher-lower-game-1.0.0.jar \
         --spring.flyway.enabled=false --spring.jpa.hibernate.ddl-auto=none \
         --spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false --wiki.refresh.enabled=false

EXPOSE 8080
# Render free tier = 512MB RAM. TieredStopAtLevel=1 / SerialGC: less JIT and GC work competing for the CPU at boot.
ENTRYPOINT ["java", "-XX:SharedArchiveFile=app.jsa", "-Xlog:cds=off", "-XX:TieredStopAtLevel=1", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=75", "-Dspring.profiles.active=prod", "-jar", "higher-lower-game-1.0.0.jar"]
