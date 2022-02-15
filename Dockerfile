FROM openjdk:11.0.11-jdk-slim as base 

ARG profile=staging

WORKDIR /app
COPY target/*.jar app.jar
COPY my_keyset.json my_keyset.json

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=$profile", "/app/app.jar"]