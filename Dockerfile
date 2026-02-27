# syntax=docker/dockerfile:1
FROM eclipse-temurin:24-jdk

WORKDIR /app

# Copy Gradle wrapper and build files first for better caching
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./

# Copy source and resources
COPY src/ src/
COPY data/ data/
COPY mappings/ mappings/
COPY ontology/ ontology/
COPY functions/ functions/

# Build the project (optional, but verifies the environment)
RUN ./gradlew build --no-daemon

CMD ["./gradlew", "run", "--no-daemon"]
