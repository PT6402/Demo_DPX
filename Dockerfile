FROM maven:3.8.3-openjdk-17 as build
WORKDIR /app
COPY . /app/
RUN mvn clean package

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
COPY /app/tokens/StoredCredential /app/tokens/StoredCredential
EXPOSE 8080
ENTRYPOINT [ "java","-jar","app.jar" ]
