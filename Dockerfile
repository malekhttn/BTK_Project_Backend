FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY target/PFE-0.0.1-SNAPSHOT.jar app.jar

# Configuration pour résoudre les problèmes de cgroups
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]