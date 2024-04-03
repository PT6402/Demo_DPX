FROM maven:3.8.3-openjdk-17 as build
WORKDIR /app
COPY . /app/
CMD ["./mvnw","spring-boot:run"]

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT [ "java","-jar","app.jar" ]
