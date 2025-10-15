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
FROM openjdk:25-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
