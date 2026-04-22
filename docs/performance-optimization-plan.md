# Mapping Performance Optimization Plan

## Context

This document describes a concrete plan to reduce runtime in the mapping phase.

The baseline for this plan is the current `-de` path, which matches the runtime of interest:

```text
./gradlew run --args='-de -dr'

Hooks                110 ms
MappingPairPlanner    38 ms
RDFMapper         65195 ms
SHACL Validation   1470 ms
Total             66813 ms
```

The conclusions below are therefore based on runs with extraction disabled and reconciliation disabled.

## Goal

Reduce wall-clock time spent in `RDFMapper` without changing RDF output semantics.

Primary focus:

1. Reduce duplicated work caused by repeated traversal of the same XML iterator.
2. Eliminate exception-heavy and warning-heavy hot paths inside mapping execution.
3. Add a safe parallel execution model for independent generated mappings.

## Non-Goals

- Rework the ontology.
- Change extraction behavior.
- Optimize SHACL validation.
- Introduce speculative abstractions unrelated to runtime reduction.

## Current Findings

### 1. Most time is in the mapper, not in planning

- `MappingPairPlanner` is effectively free relative to the total runtime.
- `RDFMapper` dominates the runtime budget.
- Disabling reconciliation does not materially change mapping time, so the bottleneck is the mapper's XML traversal and record processing.

Relevant code:

- `src/main/java/rdf/mapping/RDFMapper.java`
- `src/main/java/rdf/mapping/MappingPairPlanner.java`

### 2. Several large XML sources are traversed multiple times

The generated mapping set is small, but the source XMLs are not. In particular:

- `data/ar/atividadedeputado/xvi.xml`
- `data/ar/atividadedeputado/xvii.xml`
- `data/ar/iniciativas/xvii.xml`

Within the mapping files, the same iterator is repeated across multiple `TriplesMap`s.

Examples:

- `mappings/ar/atividadedeputado.ttl`
  - `/.../Req/RequerimentosAtivDepOut` appears twice
  - `/.../ActP/ActividadesParlamentaresOut` appears twice
  - `/.../Audiencias/ActividadesComissaoOut` appears three times
  - `/.../Audicoes/ActividadesComissaoOut` appears three times
  - `/.../Gpa/GruposParlamentaresAmizadeOut` appears three times
- `mappings/ar/informacaobase.ttl`
  - `/Legislatura/GruposParlamentares/pt_gov_ar_objectos_GPOut` appears twice
  - `/Legislatura/Deputados/DadosDeputadoOrgaoPlenario` appears twice
  - nested deputy iterators also appear twice
- `mappings/ar/registobiografico.ttl`
  - each child iterator for roles, titles, decorations, works, and habilitations appears twice

With XPath logical sources, this repeated structure is a direct candidate for wasted work.

### 3. The current mapping run spends time on failure paths

The mapper emits a large number of runtime warnings and function failures during a normal successful run.

The most visible categories are:

- `grel:string_trim` called on null values
- `ConcatFunction` warnings caused by templates evaluated with missing inputs

Examples of hot spots:

- `mappings/ar/informacaobase.ttl`
- `mappings/ar/atividadedeputado.ttl`
- `mappings/ar/registobiografico.ttl`

Examples of relevant Java functions:

- `src/main/java/rdf/mapping/functions/ToBoolean.java`
- `src/main/java/rdf/mapping/functions/ToRequisition.java`

Even when these failures do not abort the run, they still cost CPU time due to exception construction and logging.

## Constraints

### Constraint 1: relation maps are not fully avoidable when subjects differ

A pair like:

- `Parliamentarian -> didRequisition -> Requisition`
- `Requisition -> ...properties...`

usually requires two `TriplesMap`s in plain RML, because each `TriplesMap` has a single subject map.

This means the target is not "remove all relation maps".

The target is:

- keep two maps only when the subject genuinely differs
- make the relation map cheaper
- remove redundant parent-map resolution where direct object IRI generation is enough

### Constraint 2: correctness comes before speed

Every optimization step must preserve:

- graph shape
- identifiers
- links between resources
- SHACL conformance

## Plan

## Phase 1: Clean Up Hot Failure Paths

### Objective

Remove avoidable exceptions and warning-heavy behavior inside the mapper loop before making structural changes.

### Why this phase comes first

This is the lowest-risk change set and should improve both runtime and observability. It also makes later benchmarking cleaner.

### Work items

#### 1. Replace `grel:string_trim` in hot mappings with a null-safe local function

Create a local function with the behavior:

- input `null` -> output `null`
- input blank string -> output blank or `null`, depending on the current semantic need
- otherwise trim and return

Candidate implementation location:

- `src/main/java/rdf/mapping/functions/TrimSafe.java`
- corresponding FnO file under `functions/`

Then replace hot `grel:string_trim` usages in:

- `mappings/ar/atividadedeputado.ttl`
- `mappings/ar/informacaobase.ttl`
- `mappings/ar/registobiografico.ttl`

Priority is to replace the calls on fields known to be optional.

#### 2. Make custom mapping functions return `null` instead of throwing or printing

Adjust hot-path custom functions so invalid input is cheap.

Candidates:

- `src/main/java/rdf/mapping/functions/ToBoolean.java`
  - currently throws on unknown values
- `src/main/java/rdf/mapping/functions/ToRequisition.java`
  - currently prints to `stderr` on unknown values
- `src/main/java/rdf/mapping/functions/ToHabilitationLevel.java`
  - should be checked for null safety
- `src/main/java/rdf/mapping/functions/RomanNumeralConverter.java`
  - should be checked for null safety

Desired behavior:

- unexpected or missing input returns `null`
- no `System.err.println` in hot-path mapping functions
- no avoidable exceptions for normal dirty input

#### 3. Remove avoidable template warnings

Identify templates that are frequently evaluated with missing references, especially where the output is optional.

Examples include mappings that build IRIs or composite identifiers from optional parts, such as:

- `habilitation` status templates
- activity identifiers using `{ActId}`
- requisition type templates using optional input

When an output depends on required pieces, prefer a custom function that:

- checks all required inputs first
- returns `null` if any required input is missing
- builds the string only when all inputs are present

This is preferable to allowing the mapper to attempt a template and log warnings for each failed record.

### Verification

Run:

```text
./gradlew run --args='-de -dr'
```

Success criteria:

- `RDFMapper` time improves measurably
- mapper logs no longer contain large volumes of trim failures
- mapper logs contain materially fewer template warnings
- SHACL still conforms
- Total `output/graph.ttl` size stays the same

### Expected impact

Moderate. This is unlikely to cut the runtime in half by itself, but it should be one of the cheapest wins to implement.

## Phase 2: Reduce Repeated Iterator Traversal

### Objective

Reduce repeated scans over the same XPath iterator without breaking relation semantics.

### Important clarification

The optimization target is not "delete relation maps".

The optimization target is:

- merge maps only when the subject is the same
- keep separate maps when the subject differs
- replace `rr:parentTriplesMap` object resolution with direct object IRI templates when possible

### Work items

#### 1. Audit each repeated iterator group

For each duplicated iterator in:

- `mappings/ar/atividadedeputado.ttl`
- `mappings/ar/informacaobase.ttl`
- `mappings/ar/registobiografico.ttl`

classify each group into one of these buckets:

1. Same iterator, same subject
2. Same iterator, different subjects, but relation object can be emitted directly
3. Same iterator, different subjects, and parent map is still necessary

This audit should be done first so changes stay surgical.

#### 2. Merge maps when iterator and subject are both the same

If two `TriplesMap`s:

- use the same `rml:iterator`
- produce the same subject IRI

then they should usually become one `TriplesMap` with additional `rr:predicateObjectMap`s.

This avoids a redundant pass over the same records.

#### 3. Replace `rr:parentTriplesMap` with direct object templates where possible

For patterns like:

- relation map subject: `Parliamentarian_{...}`
- relation object: points to a resource whose IRI is already fully derivable from the current record

prefer:

```ttl
rr:objectMap [
  rr:template "http://purl.org/polis/ar/graph#Requisition_{ReqId}" ;
  rr:termType rr:IRI
]
```

instead of:

```ttl
rr:objectMap [
  rr:parentTriplesMap <#RequisitionMap> ;
]
```

This keeps the relation map but makes it cheaper.

Candidate areas:

- requisitions
- parliamentary activities
- hearings
- auditions
- friendship parliamentary groups
- delegation meeting relations

#### 4. Remove entity maps that do not add information

If a target entity map contributes no independent facts beyond the IRI already used in relations, delete it.

This only applies when:

- the resource is referenced but has no properties of its own
- or all of its facts can be emitted in a map that already exists for the same subject

This step must be applied conservatively.

### Verification

For each iterator group refactor:

1. Run the mapping with `-de -dr`
2. Compare the resulting graph size and spot-check key identifiers
3. Run SHACL validation

Success criteria:

- fewer repeated iterator declarations in the edited mapping file
- same RDF semantics
- lower `RDFMapper` runtime

### Expected impact

High. This is the most likely structural improvement because the current mapping files repeatedly traverse the same subtrees in the largest XML files.

## Phase 3: Add Parallel Mapping Execution

### Objective

Reduce wall-clock time by executing independent generated mappings in parallel.

### Why this is phase 3

Parallel execution adds complexity. It should be done after the mapping definitions stop spending time on noisy failure paths, otherwise we parallelize inefficiency and make diagnosis harder.

### Current execution model

`src/main/java/rdf/mapping/RDFMapper.java` currently:

1. loads all generated mappings into one RML store
2. creates one `RecordsFactory`
3. creates one `Agent`
4. runs one `Executor`
5. writes one output graph

This is simple but fully serial.

### Proposed execution model

Run each generated mapping file as an isolated mapper job.

Candidate partitioning:

- one job per generated temporary mapping file
- or one job per source family if memory pressure becomes too high

For the current generated set, that means jobs such as:

- `tmp/mappings/ar/atividadedeputado/xvi.ttl`
- `tmp/mappings/ar/atividadedeputado/xvii.ttl`
- `tmp/mappings/ar/informacaobase/xvi.ttl`
- `tmp/mappings/ar/informacaobase/xvii.ttl`
- `tmp/mappings/ar/registobiografico/xvi.ttl`
- `tmp/mappings/ar/registobiografico/xvii.ttl`

Each job should:

1. create its own `QuadStore`
2. create its own `Agent`
3. create its own `Executor`
4. execute independently
5. write its own temporary RDF output file

Then the main process should:

1. load all temporary RDF outputs
2. merge them into one final store
3. write the final output graph

### Design rules

- Do not share `Executor`, `Agent`, `QuadStore`, or mutable stores across threads.
- Treat each mapper run as fully isolated.
- Keep merge logic single-threaded initially.
- Preserve namespace copying in the final output.

### Suggested implementation steps

#### 1. Refactor `RDFMapper` into smaller units

Split responsibilities in `src/main/java/rdf/mapping/RDFMapper.java`:

- load one mapping file
- run one isolated execution
- write one temporary RDF file
- merge RDF files into final output

This refactor should happen before adding concurrency primitives.

#### 2. Introduce a per-job result model

Create a small internal result object that records:

- source mapping path
- temporary output path
- execution duration
- success or failure

This will make benchmarking and debugging much easier.

#### 3. Execute with a bounded thread pool

Use a bounded pool sized from available processors, with a conservative default.

Good initial rule:

- `min(number of mapping jobs, max(2, availableProcessors / 2))`

Do not start with an aggressive thread count.

#### 4. Merge outputs after all jobs complete

Use RDF4J to load each job result and write the final merged graph.

#### 5. Keep a sequential fallback

Add a configuration switch or code path that preserves the current serial behavior for comparison and debugging.

### Verification

Compare:

```text
./gradlew run --args='-de -dr'
```

before and after the parallel implementation.

Success criteria:

- output graph is equivalent for the same inputs
- SHACL still conforms
- wall-clock time drops materially on a multi-core machine
- memory usage stays acceptable
- failures are isolated to individual job outputs

### Expected impact

Potentially high for wall-clock time, but dependent on:

- core count
- memory pressure
- how uneven the mapping file costs are

This phase is best treated as an optimization of elapsed time, not total CPU consumed.

## Recommended Order of Execution

1. Phase 1
   Verify: run `./gradlew run --args='-de -dr'` and confirm warning volume drops.
2. Phase 2
   Verify: compare graph semantics and runtime after each iterator-group refactor.
3. Phase 3
   Verify: compare sequential and parallel output and measure wall-clock improvement.

This order is intentional:

- first remove noisy hot-path waste
- then reduce duplicated structural work
- then parallelize the remaining real work

## Measurement Plan

Use the current benchmark summary already printed by the application.

For each phase:

1. Run `./gradlew run --args='-de -dr'` three times.
2. Record `RDFMapper` time.
3. Record total runtime.
4. Record whether SHACL conforms.
5. Record whether mapper logs still show bulk warning/error patterns.

Track the numbers in a simple table in this document or in a follow-up benchmark file.

Suggested columns:

- date
- commit
- phase
- RDFMapper ms
- total ms
- notes

## Risks

### Risk 1: semantic drift during map consolidation

Merging or deleting maps too aggressively can silently change graph shape.

Mitigation:

- change one iterator group at a time
- verify output after each change

### Risk 2: overfitting to current data shape

Some optional fields may only appear in future legislatures or new XML variants.

Mitigation:

- prefer null-safe behavior over exception-based assumptions
- keep functions permissive where input quality is variable

### Risk 3: parallel merge bugs

Parallel generation followed by merge can introduce duplicate namespace or output ordering differences.

Mitigation:

- compare semantic graph content, not textual line order
- keep merge code simple

### Risk 4: memory pressure in parallel mode

Multiple mapper jobs may increase heap usage sharply.

Mitigation:

- start with a small thread pool
- profile on realistic input sizes before increasing concurrency

## Immediate Next Steps

If work starts now, the most pragmatic sequence is:

1. add a null-safe trim function and replace the hottest `grel:string_trim` sites
2. make hot custom functions non-throwing and non-printing
3. refactor one repeated iterator group in `mappings/ar/atividadedeputado.ttl`
4. re-measure
5. only after that, begin the `RDFMapper` parallelization refactor

## Files Most Likely To Change

- `mappings/ar/atividadedeputado.ttl`
- `mappings/ar/informacaobase.ttl`
- `mappings/ar/registobiografico.ttl`
- `src/main/java/rdf/mapping/RDFMapper.java`
- `src/main/java/rdf/mapping/functions/ToBoolean.java`
- `src/main/java/rdf/mapping/functions/ToRequisition.java`
- new null-safe mapping function classes under `src/main/java/rdf/mapping/functions/`
- new FnO descriptions under `functions/`
