FROM openjdk:11-jre-slim
EXPOSE 8059
ADD target/waya-authentication-service-0.0.1-SNAPSHOT.jar waya-authentication-service.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/waya-authentication-service.jar"]

