FROM maven:3.2.4-openjdk-17 as build
WORKDIR /app
COPY . /app/
RUN mvn clean package

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT [ "java","-jar","app.jar" ]
