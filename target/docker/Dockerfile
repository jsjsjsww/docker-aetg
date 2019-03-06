FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD acts_3.0.jar acts_3.0.jar
ADD docker-acts-1.0.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]