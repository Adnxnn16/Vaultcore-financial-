# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 -S vaultcore && \
    adduser -S vaultcore -u 1001 -G vaultcore

COPY --from=build /app/target/*.jar app.jar

USER vaultcore
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
