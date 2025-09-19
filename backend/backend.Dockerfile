# Stage 1: Build the application with Maven
FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code and build the application JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create a slim final image with only the JRE and the app JAR
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the application JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the backend runs on
EXPOSE 8080

# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
