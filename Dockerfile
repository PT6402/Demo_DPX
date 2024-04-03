FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --chown=appuser:appuser target/**.jar /home/appuser/app.jar
ENTRYPOINT [ "java","-jar","/app.jar" ]
EXPOSE 8080
