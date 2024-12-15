FROM openjdk:23

COPY ./html /home/arun/Work/my-load-balancer/html
WORKDIR /app
COPY target/my-load-balancer-1.0-SNAPSHOT.jar app.jar
EXPOSE 9000
CMD ["java", "-jar", "app.jar"]
