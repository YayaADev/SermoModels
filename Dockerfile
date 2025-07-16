# SermoModels/Dockerfile
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Create a lightweight image with just the JAR
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar models.jar
CMD ["echo", "SermoModels container - models.jar available"]