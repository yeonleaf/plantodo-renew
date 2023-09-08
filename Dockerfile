FROM openjdk:17-ea-11-jdk-slim
RUN ls -R
COPY ./plantodo/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
