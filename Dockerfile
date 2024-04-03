# Base image được sử dụng để build image
FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
COPY .mvn/ .mvn
COPY mvnw pom.xml ./ 
EXPOSE 8080
COPY src ./src 
CMD ["./mvnw","spring-boot:run"]