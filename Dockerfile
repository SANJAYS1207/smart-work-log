# =============================
# Smart Work Log Tracker
# =============================
# Build the JAR first: mvn clean package -DskipTests
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests 2>/dev/null || \
    (apk add --no-cache maven && mvn clean package -DskipTests)

# Production image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/smartlog-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
