FROM openjdk:11.0.11-jdk-slim as base 
WORKDIR /app
COPY target/*.jar app.jar
COPY my_keyset.json my_keyset.json
COPY src/main/resources/application.yml application.yml
COPY src/main/resources/application-staging.yml application-staging.yml
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=staging", "/app/app.jar"]
