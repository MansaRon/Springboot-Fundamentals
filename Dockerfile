FROM openjdk:17
WORKDIR /app
COPY target/*.jar full-stack-springboot-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/full-stack-springboot-0.0.1-SNAPSHOT.jar"]
