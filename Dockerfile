FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/swacch-app.jar swacch-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "swacch-app.jar"]
