FROM openjdk:17-ea-11-jdk-slim
SHELL ["find", ".", "-name", "*.jar"]
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
