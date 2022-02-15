FROM openjdk:11.0.11-jdk-slim as base 

WORKDIR /app
COPY target/*.jar app.jar
COPY my_keyset.json my_keyset.json

ENTRYPOINT ["java", "-jar", "/app/app.jar"]