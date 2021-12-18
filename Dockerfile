FROM adoptopenjdk/openjdk11:alpine-jre as base

WORKDIR /app
RUN addgroup -S waya && adduser -S waya -G waya
USER waya:waya
COPY target/*.jar app.jar
COPY my_keyset.json my_keyset.json
COPY src/main/resources/application.yml application.yml

ENTRYPOINT ["java","-jar","/app/app.jar"]
