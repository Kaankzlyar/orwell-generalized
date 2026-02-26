# syntax=docker/dockerfile:1
FROM eclipse-temurin:24-jdk

WORKDIR /app

# System deps for downloading the mapper
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl ca-certificates \
  && rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper and build files first for better caching
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./

# Copy source and resources
COPY src/ src/
COPY data/ data/
COPY mappings/ mappings/
COPY ontology/ ontology/
COPY functions/ functions/

# Fetch RMLMapper 8.1 and place it in lib/
RUN mkdir -p lib/ && curl -fsSL -o lib/rmlmapper-8.1.0-r380-all.jar \
    https://github.com/RMLio/rmlmapper-java/releases/download/v8.1.0/rmlmapper-8.1.0-r380-all.jar

# Build the project (optional, but verifies the environment)
RUN ./gradlew build --no-daemon

CMD ["./gradlew", "run", "--no-daemon"]
