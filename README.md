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

## Data Extraction

Orwell uses an extensible Data Extractor framework. Each data source has its own extractor that handles source-specific JSON structures.

### Source Configuration

Each source has a JSON file in the `sources/` directory:

```jsonc
// sources/ar.json
{
  "informacaobase": {
    "XVII": "https://www.parlamento.pt/.../some.xml",
    "XVI": "https://www.parlamento.pt/.../some-other.xml"
  },
  "iniciativas": {
    "XVII": "https://www.parlamento.pt/.../another.xml"
  }
}
```

The `DataExtractor` abstract class provides common functionality (HTTP fetching, file storage, Content-Type-based extension detection). Each extractor extends this class and implements `parseSources()` to handle its specific JSON structure.

After extraction, the data folder structure mirrors the source JSON:

```txt
data/
|-ar/
| |-informacaobase/
| | |-xvii.xml
| | |-xvi.xml
| |
| |-iniciativas/
|   |-xvii.xml
```

### Adding a New Source

1. Create a JSON file in `sources/` (e.g., `sources/base.json`)
2. Create a new extractor class extending `DataExtractor`
3. Implement `SOURCE_PATH()` to return the path to your JSON file
4. Implement `parseSources()` to build a `SourceNode` tree from your JSON structure
5. Add the extractor to the list in `Main.java`

## Mapping

Mappings are authored in Turtle under `mappings/<source>/` and may use dynamic reconciliation through FnML/FnO. Each source has its own mapping subdirectory matching the data folder structure:

```txt
mappings/
|-ar/
| |-informacaobase.ttl
| |-iniciativas.ttl
```

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
