FROM adoptopenjdk/openjdk11:alpine-jre as base

WORKDIR /app
RUN addgroup -S waya && adduser -S waya -G waya
USER waya:waya
COPY target/*.jar app.jar
COPY my_keyset.json my_keyset.json
COPY src/main/resources/application.yml application.yml
COPY src/main/resources/application-staging.yml application-staging.yml

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=staging", "/app/app.jar"]
