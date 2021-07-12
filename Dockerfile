FROM adoptopenjdk/openjdk16:x86_64-alpine-jdk-16.0.1_9    
# RUN apk add --no-cache fontconfig
# RUN ln -s /usr/lib/libfontconfig.so.1 /usr/lib/libfontconfig.so && \
#     ln -s /lib/libuuid.so.1 /usr/lib/libuuid.so.1 && \
#     ln -s /lib/libc.musl-x86_64.so.1 /usr/lib/libc.musl-x86_64.so.1
# ENV LD_LIBRARY_PATH /usr/lib
# RUN apt-get update
# RUN apt-get install -y fontconfig
# RUN apt-get install libfontconfig1
# RUN apt-get install libfreetype6
EXPOSE 8059
ADD target/waya-authentication-service-0.0.1-SNAPSHOT.jar waya-authentication-service.jar
ENTRYPOINT ["java","-Dspring.profiles.active=test", "-jar", "/waya-authentication-service.jar"]

