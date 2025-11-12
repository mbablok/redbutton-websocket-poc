FROM eclipse-temurin:25

WORKDIR /app

COPY target/scala-3.3.6/redbutton-websocket-poc-assembly-0.0.1-SNAPSHOT.jar redbutton-websocket-poc.jar

EXPOSE 8080

CMD ["java", "-jar", "redbutton-websocket-poc.jar"]
