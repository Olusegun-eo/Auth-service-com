FROM openjdk:11-jre-slim
EXPOSE 8059
ADD target/*.jar waya-authentication-service.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/waya-authentication-service.jar"]


