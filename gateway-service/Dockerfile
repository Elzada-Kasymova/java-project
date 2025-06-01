FROM openjdk:17-jdk-slim
LABEL authors="kasymovaelzada"

VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
