# Mapping Source Refactor

## Problem

Mapping files under `mappings/` grow very large (2k+ LOC in `iniciativas.ttl` and `registobiografico.ttl`) because they cannot be split across multiple files. Currently, each mapping file must match a single data subdirectory:

```
mappings/ar/iniciativas.ttl  →  data/ar/iniciativas/*.xml
```

The file name (`iniciativas`) determines the data directory. This prevents splitting `iniciativas.ttl` into smaller domain-specific files like `petition.ttl` while still targeting the same `data/ar/iniciativas/` directory.

## Solution

Replace the filename-based data directory lookup with an **explicit source name** declared in each `rml:logicalSource` block via the `rml:source` predicate.

### Convention

Instead of:

```turtle
rml:logicalSource [
    rml:source "data/" ;
    ...
] ;
```

Write the **data subdirectory name** directly:

```turtle
rml:logicalSource [
    rml:source "iniciativas" ;
    ...
] ;
```

The mapping engine resolves the path as:

```
{Config.DATA_DIR}/{domain}/{source-name}/
```

where `domain` is the parent directory of the mapping file (e.g., `ar`) and `source-name` is the `rml:source` value.

This allows:

- `mappings/ar/petition.ttl` with `rml:source "iniciativas"` → reads `data/ar/iniciativas/*.xml`
- `mappings/ar/iniciativas.ttl` unchanged → continues reading `data/ar/iniciativas/*.xml`

### Backward Compatibility

Existing mapping files use `rml:source "data/"`. The value `"data/"` is special-cased: if **all** logical sources in a file use `"data/"`, the engine falls back to the old filename-based lookup (`data/{extractor}/{mapping-name}/`). This ensures existing files need zero changes.

If a file mixes `"data/"` with explicit names, only the explicit names are used for directory resolution (the `"data/"` sources still get their `rml:source` replaced with absolute paths in the generated output).

### Algorithm

1. Read the mapping template file
2. Extract all unique `rml:source` string values using regex
3. Filter out the literal `"data/"` placeholder
4. If no explicit names remain → fall back to `data/{extractor}/{mapping-name}/` (old behavior)
5. Otherwise, for each explicit name → resolve as `data/{extractor}/{name}/`
6. Collect all XML files from the resolved directories
7. For each XML file, generate a temp mapping (replacing all `rml:source` values with the XML's absolute path, as before)

### Changes to `MappingPairPlanner.java`

- Add `extractSourceNames(String)` — returns unique `rml:source` values from a mapping template
- Add `resolveDataDirectories(List<String>, String, String)` — maps source names to paths
- Add `collectXmlFiles(List<Path>)` — collects XML files from multiple directories
- Modify `createPairsForMapping` — use new resolution flow instead of deriving path from mapping name only
- The `createMappingFile` and `SOURCES_PATTERN` replacement logic is unchanged

### Changes to `MappingPairPlannerTest.java`

- Update existing tests to use `rml:source "test"` (matching the mapping name) instead of `rml:source "dummy.xml"`
- Add a test demonstrating cross-reference (mapping name differs from source name)
- The `createMappingPairsThrowsWhenAllMappingsSkipped` test continues to pass unchanged

### Example

```turtle
# mappings/ar/petition.ttl
@prefix ... .

<#PetitionMap> a rr:TriplesMap ;
    rml:logicalSource [
        rml:source "iniciativas" ;     # ← explicit source directory
        rml:referenceFormulation ql:XPath ;
        rml:iterator "//Petitions/..." ;
    ] ;
    rr:subjectMap [ ... ] ;
    ...
```

The planner resolves `rml:source "iniciativas"` as `data/ar/iniciativas/`, finds all XML files there, and generates per-file temp mappings as before.
