# Build stage (compile + package)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package

# Run stage (runtime only)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/maven-docker-sample-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
