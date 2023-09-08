FROM openjdk:17-ea-11-jdk-slim
COPY build/libs/*.jar app.jar
RUN ["ls", "-al"]
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
