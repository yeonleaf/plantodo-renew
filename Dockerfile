FROM openjdk:17-ea-11-jdk-slim
RUN ["ll" "/usr"]
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
