FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Expect a pre-built JAR in the target/ directory (built locally by Maven)
COPY target/order-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
