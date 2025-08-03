FROM openjdk:17-jdk-slim
WORKDIR /app
COPY /target/PFE-0.0.1-SNAPSHOT.jar PFE-0.0.1-SNAPSHOT.jar
EXPOSE 8010
ENTRYPOINT ["java", "-jar", "PFE-0.0.1-SNAPSHOT.jar"]