#FROM maven:3.9.5-eclipse-temurin-17 AS builder
#WORKDIR /app
#COPY pom.xml .
#RUN mvn dependency:go-offline # Cache dependencies first
#COPY src/ ./src/
#RUN mvn clean package -DskipTests

#FROM openjdk:25-jdk-slim 
#WORKDIR /app
#COPY --from=builder /app/target/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","app.jar"]
#FROM openjdk:21-jdk-slim
FROM openjdk:26-ea-jdk-slim
LABEL maintainer="appupiratla@gmail.com"
LABEL version="1.0"
LABEL description="FineFlux Spring Boot application"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
