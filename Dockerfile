FROM amazoncorretto:17-alpine

# curl bruges af healthchecket i docker-compose
RUN apk update && apk add --no-cache curl

COPY target/app.jar /app.jar
EXPOSE 7070
CMD ["java", "-jar", "/app.jar"]
