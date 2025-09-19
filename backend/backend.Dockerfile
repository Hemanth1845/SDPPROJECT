# Use official OpenJDK image
FROM openjdk:17-jdk-slim

# Set workdir
WORKDIR /app

# Copy jar from target (after mvn package or gradle build)
COPY target/*.jar app.jar

# Expose backend port
EXPOSE 2000

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
