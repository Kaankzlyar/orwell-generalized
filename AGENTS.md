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

# Guidelines

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.