FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Spring Boot JAR from target (after mvn/gradle build)
COPY target/*.jar app.jar

EXPOSE 2000
ENTRYPOINT ["java", "-jar", "app.jar"]
