# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests && rm -rf /root/.m2/repository

# Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/profile-0.0.1-SNAPSHOT.war profile.war

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "profile.war"]
