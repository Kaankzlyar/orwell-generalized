# Orwell

Extracts information from Portuguese open government data sources and generates an RDF Graph, following the ontology in `ontology/`.

## Prerequisites

- **Local:** JDK 21+
- **Docker:** Docker installed
- Internet access on first build (Gradle dependencies)

## Build & Run

### Local

```sh
./gradlew build        # Compile and package
./gradlew run          # Run the pipeline
./gradlew test         # Run tests
```

### Docker

```sh
docker build -t orwell .
docker run --rm orwell
```

Keep generated output on the host:

```sh
mkdir -p output
docker run --rm -v "$(pwd)/output:/app/output" orwell
```

## CLI Flags

All flags accept both short (`-dr`) and long (`--disable-reconciliation`) forms. Pass `--help` for usage.

| Short | Long | Description |
|-------|------|-------------|
| `-dr` | `--disable-reconciliation` | Skip Wikidata reconciliation |
| `-de` | `--disable-extraction` | Skip data extraction phase |
| `-dm` | `--disable-mapping` | Skip RDF mapping phase |
| `-ds` | `--disable-shacl` | Skip SHACL validation |
| `-df` | `--disable-shacl-failure` | Don't throw on SHACL violation |
| `-r` | `--disable-shacl-report` | Suppress SHACL validation report |
| `-l` | `--enable-log` | Log reconciliation requests to `log.txt` |
| `-t` | `--keep-tmp` | Keep temporary files after processing |
| `-q` | `--enable-queries` | Run SPARQL queries on the final graph |
| `-f` | `--enable-fuseki` | Push final graph to a Fuseki server |
| `-h` | `--help` | Show usage and exit |

Example:

```sh
./gradlew run --args="-dr"                        # disable reconciliation
./gradlew run --args="-de --disable-mapping"       # extraction + mapping off
./gradlew run --args="--help"                      # print usage
./gradlew run --args="-f"                          # push to Fuseki after pipeline
FUSEKI_URL=http://host:3030/ds ./gradlew run --args="-f"   # custom Fuseki target
```

## Configuration

### Pipeline options

Boolean flags are set via CLI (see above). There are no static config fields — options are read from `cli.Options` across the codebase.

### `config.yml`

Controls which legislatures are processed:

```yaml
legislatures:
    - code: xiv
      enabled: true
    - code: xv
      enabled: true
    - code: xvi
      enabled: true
```

Set `enabled: false` to skip a legislature without removing its data.

### Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `FUSEKI_URL` | `http://localhost:3030/ds` | Fuseki dataset endpoint for `--enable-fuseki` |
| `FUSEKI_PORT` | `3030` | Fuseki server port for `--enable-fuseki` |

Set them inline or via a `.env` file:

```sh
export ORWELL_FUSEKI_URL=http://server:3030/mydataset
./gradlew run --args="--enable-fuseki"
```

### Data sources (`sources/`)

Each JSON file defines source URLs per legislature:

```json
{
  "informacaobase": {
    "XVII": "https://www.parlamento.pt/.../some.xml",
    "XVI": "https://www.parlamento.pt/.../some-other.xml"
  }
}
```

## Pipeline

1. **Extraction** — Downloads source files from configured URLs into `data/`.
2. **Preprocessing** — Runs registered hooks (parliamentarian reconciliation, commission info, voting extraction).
3. **Mapping** — RMLMapper processes Turtle mapping files from `mappings/` against extracted data. Supports FnO/FnML functions in `functions/` and Wikidata reconciliation.
4. **Graph assembly** — Loads per-legislature graphs into a unified model.
5. **SHACL validation** — Validates the final graph against shapes in `shacl/`.
6. **Fuseki push** *(optional)* — Loads the model into a running Apache Jena Fuseki server (`--enable-fuseki`).

## Data Extraction

Each source extends `DataExtractor`. To add a new source:

1. Create a JSON config in `sources/`.
2. Implement an extractor extending `DataExtractor` (implement `SOURCE_PATH()` and `parseSources()`).
3. Register it in `Main.extract()`.

Extracted files land in `data/<domain>/<resource>/<legislature>.<ext>`. For example: `data/ar/iniciativas/<legislature>.xml`

## Mapping

Mappings live under `mappings/<domain>/` in Turtle. They reference data files from the extraction step and may call reconciliation functions. The planner pairs each data directory with its corresponding mapping files, generating combined mapping documents in `tmp/`.

Query patterns:

```ttl
# Direct reference
rr:objectMap [ rml:reference "cpDes" ] ;

# Composed string
rr:objectMap [
    rr:template "Círculo {cpDes}" ;
    rr:datatype xsd:string
] ;
```

## Output

- **Graph:** `output/graph-<legislature>.ttl` (one per legislature)
- **Cache:** `reconciliation-cache.properties` (persisted across runs)
- **Log:** `log.txt` (when `-l` is enabled)
- **Temp files:** `tmp/` (cleaned up on success)

## Fuseki

The `--enable-fuseki` flag pushes the final graph into an Apache Jena Fuseki server for interactive SPARQL querying.

### Setup

Download and start Fuseki:

```sh
wget https://dlcdn.apache.org/jena/binaries/apache-jena-fuseki-6.1.0.tar.gz
tar xzf apache-jena-fuseki-6.1.0.tar.gz
cd apache-jena-fuseki-6.1.0
./fuseki-server --mem /ds
```

Then in another terminal:

```sh
cd /path/to/orwell
./gradlew run --args="--enable-fuseki"
```

### Server deployment

Set `ORWELL_FUSEKI_URL` to point to a remote Fuseki instance:

```sh
export ORWELL_FUSEKI_URL=http://your-server:3030/mydataset
./gradlew run --args="--enable-fuseki"
```

The web UI is available at `http://your-server:3030` and the SPARQL endpoint at `http://your-server:3030/mydataset/sparql`.
