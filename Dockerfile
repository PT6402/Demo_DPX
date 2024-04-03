# Base image được sử dụng để build image
FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install openjdk-17-jdk -y

COPY .mvn/ .mvn
COPY mvnw pom.xml ./ 
COPY src ./src 
RUN ./mvnw spring-boot:run

FROM openjdk:17-jdk-slim
EXPOSE 8080

