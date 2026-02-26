# Orwell

Big Brother is watching you.

## Prerequisites

### Run without Docker
- JDK 21+ installed
- Internet access on first build (Gradle dependencies)
- RMLMapper JAR at `lib/rmlmapper-8.1.0-r380-all.jar`

Download RMLMapper 8.1.0:

```sh
mkdir -p lib
curl -fsSL -o lib/rmlmapper-8.1.0-r380-all.jar \
  https://github.com/RMLio/rmlmapper-java/releases/download/v8.1.0/rmlmapper-8.1.0-r380-all.jar
```

### Run with Docker
- Docker installed

No manual RMLMapper setup is required in Docker (the image downloads it).

## Build and run (without Docker)

Build:

```sh
./gradlew build
```

Run:

```sh
./gradlew run
```

Run without reconciliation HTTP calls:

```sh
./gradlew run --args="--disable-reconciliation"
```

Generated artifacts:
- RDF graph: `output/graph.ttl`
- Temporary generated mappings: `tmp/` (cleaned up at the end of a successful run)

## Mapping

Mappings are authored in Turtle under `mappings/` and may use dynamic reconciliation through FnML/FnO.

When passing the reconciliation `query` argument, use one of these patterns:

1. Raw value from XML (`rml:reference`)

```ttl
rr:predicateObjectMap [
  rr:predicate ex:query ;
  rr:objectMap [ rml:reference "cpDes" ]
] ;
```

2. Composed query string (`rr:template`)

```ttl
rr:predicateObjectMap [
  rr:predicate ex:query ;
  rr:objectMap [
    rr:template "Círculo {cpDes}" ;
    rr:termType rr:Literal ;
    rr:datatype xsd:string
  ]
] ;
```

Use `rml:reference` when the source value is already suitable. Use `rr:template` when you need disambiguation/context in the final query string.

## Build and run (Docker)

Build image:

```sh
docker build -t orwell .
```

Run container:

```sh
docker run --rm orwell
```

To keep the generated output on your host machine:

```sh
mkdir -p output
docker run --rm -v "$(pwd)/output:/app/output" orwell
```
