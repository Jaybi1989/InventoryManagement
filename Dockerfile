FROM openjdk:21-jdk-slim

RUN mkdir -p /machinepark
WORKDIR /machinepark

COPY target/inventory-1.0-SNAPSHOT.jar app.jar

ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/machinepark_inventory
ENV SPRING_DATASOURCE_USERNAME=app_user
ENV SPRING_DATASOURCE_PASSWORD=app_password

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/machinepark/app.jar"]
