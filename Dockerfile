# ---- Build stage: compile the Spring Boot jar with Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Cache dependencies first (only re-downloads when pom.xml changes)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests clean package

# ---- Run stage: small runtime image with just the jar ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/hospital-1.0.0.jar app.jar
EXPOSE 8080
# Use the postgres profile when running under Docker Compose.
ENV SPRING_PROFILES_ACTIVE=postgres
ENTRYPOINT ["java", "-jar", "app.jar"]
