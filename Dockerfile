##FROM openjdk:11-jre-slim
##EXPOSE 8059
##ADD target/*.jar waya-authentication-service.jar
##ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/waya-authentication-service.jar"]
FROM openjdk:13-jdk-alpine as base
WORKDIR /app
RUN addgroup -S waya && adduser -S waya -G waya
USER waya:waya
COPY target/*.jar app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=staging","-jar","/app/app.jar"]
