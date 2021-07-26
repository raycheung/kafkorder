FROM openjdk:8-alpine

COPY target/uberjar/kafkorder.jar /kafkorder/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/kafkorder/app.jar"]
