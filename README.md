# Orwell

Big Brother is watching you.

## Prerequisites

### Run without Docker
- JDK 21+ installed
- Internet access on first build (Gradle dependencies)

### Run with Docker
- Docker installed

No manual RMLMapper setup is required. It is pulled as a Gradle dependency.

## Build

### Manually

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

## Build

### Docker

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