# Per-Legislature Graph Generation

## Motivation
Previously, the pipeline produced a single monolithic RDF graph (`output/graph.ttl`) containing data for all legislatures. This made downstream consumption harder — a client interested only in legislature `xvii` had to parse everything.

Now the pipeline produces one output file per legislature (e.g. `output/graph-xvii.ttl`, `output/graph-xvi.ttl`), and which legislatures to process is controlled by `config.yml`.

## How it works

### 1. Configuration (`config.yml`)
Located at the project root. Lists legislatures and whether they are explicitly disabled:

```yaml
legislatures:
  - code: xv
    enabled: false
  - code: xiv
    enabled: false
```

**Rule**: a legislature is enabled unless it appears in the config with `enabled: false`. Legislatures not listed (e.g. new ones added to `sources/ar.json`) default to enabled. This means adding a new legislature requires no config change.

### 2. Data file naming
XML data files are named by legislature code: `xvii.xml`, `xvi.xml`, etc. This is consistent across all source directories (`informacaobase/`, `iniciativas/`, etc.), which enables future multi-source mapping pairing by common filename.

### 3. Extraction (`ARExtractor`)
When parsing `sources/ar.json`, entries for disabled legislatures are skipped entirely — no data is downloaded or stored for them.

### 4. Planner (`MappingPairPlanner`)
`createMappingPairs()` now returns `Map<String, List<Path>>` — a map from legislature code to the list of generated temp mapping files for that legislature.

Within `createPairsForMapping`, the common base filenames (which are legislature codes) are filtered against the disabled set. Only enabled legislatures generate temp mappings.

### 5. Mapper (`RDFMapper`)
Accepts an optional `outputPath` field. When set, the mapper writes to that path instead of the default `Config.OUTPUT_PATH`.

### 6. Main pipeline (`Main.java`)
After the planner returns the per-legislature map, the main loop iterates over each entry and runs a separate `RDFMapper` for each legislature, writing to `output/graph-{legislature}.ttl`.

- Preprocessing hooks still run once on all data (unchanged).
- SHACL validation currently runs only on the default `output/graph.ttl` path; it is skipped when per-legislature output is produced, since no combined file exists.

## File changes

| File | Change |
|---|---|
| `config.yml` | New — controls which legislatures are enabled |
| `src/main/java/config/ConfigParser.java` | New — reads `config.yml` into `Set<String>` of disabled codes |
| `src/main/java/config/Config.java` | Added `DISABLED_LEGISLATURES` field, `legislatureOutputPath()` helper |
| `src/main/java/extraction/ARExtractor.java` | Filter disabled legislatures during source parsing |
| `src/main/java/rdf/mapping/MappingPairPlanner.java` | Return `Map<String, List<Path>>`, filter by disabled set |
| `src/main/java/rdf/mapping/RDFMapper.java` | Added `outputPath` field for per-legislature output |
| `src/main/java/Main.java` | Iterate per-legislature map, run RDFMapper per legislature |
| `src/test/java/rdf/mapping/MappingPairPlannerTest.java` | Updated for new return type |
| `build.gradle.kts` | Added `snakeyaml` dependency |
