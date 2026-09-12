FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Expect a pre-built JAR in the target/ directory (built locally by Maven)
COPY target/restaurant-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
