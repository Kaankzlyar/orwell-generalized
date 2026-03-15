# Repository Guidelines

## Project Overview

- This project revolves around Web Semantics and Linked Data.
- This project's aim is to extract information from portuguese open government data sources and generate an RDF Graph from it, following the ontology in `ontology/`.
- The main goal is to provide a better, more organized source of governmental data for third parties, such as mobile apps and websites.

## Project Structure & Module Organization

- `src/main/java/`: Core Java sources. Entry point is `Main.java`.
- `src/test/java/`: JUnit tests (currently `*Test.java`).
- `mappings/`: Turtle mapping files used by the RML pipeline.
- `functions/`: FnO/FnML functions referenced by mappings.
- `ontology/`, `shacl/`: Ontology and SHACL validation assets.
- `data/`: Input data used during mapping.
- `output/`: Generated RDF output (e.g., `output/graph.ttl`).
- `tmp/`: Temporary mappings generated at runtime (cleaned on success).

## Build, Test, and Development Commands

- `./gradlew build`: Build the project (tests are skipped by default).
- `./gradlew run`: Run the pipeline locally.
- `./gradlew run --args="--disable-reconciliation"`: Run without HTTP reconciliation calls.
- `./gradlew test`: Run JUnit tests.
- `./gradlew clean`: Clean build outputs and `log.txt`.
- `docker build -t orwell .` and `docker run --rm orwell`: Build and run via Docker.

## Coding Style & Naming Conventions

- Java indentation uses 4 spaces; no tabs in existing sources.
- Classes use `PascalCase`, methods/fields `camelCase`, packages lowercase.
- Tests follow `*Test.java` naming under `src/test/java/`.
- No repo-wide formatter is configured; keep style consistent with nearby files.

## Testing Guidelines

- Framework: JUnit Jupiter (JUnit 5) via Gradle.
- Run tests with `./gradlew test`. Tests do not run as part of `./gradlew build`.
- Add new tests alongside the relevant package structure under `src/test/java/`.

## Configuration & Runtime Notes

- Requires JDK 21+ for local runs.
- Reconciliation uses a cache persisted on successful runs; avoid deleting `reconciliation-cache.properties` unless intended.
