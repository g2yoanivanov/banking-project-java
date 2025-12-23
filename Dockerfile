FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN apk update && apk upgrade --no-cache

COPY target/banking-project-1.0-SNAPSHOT.jar app.jar

EXPOSE 6262

ENTRYPOINT ["java", "-jar", "app.jar"]