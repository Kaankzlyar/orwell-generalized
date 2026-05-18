# Benchmark Scripts

These scripts benchmark the Orwell pipeline layers reported by `utils.Benchmark`.
They do not require Java code changes.

Run the full pipeline 10 times and average each layer:

```sh
python3 scripts/benchmark_layers.py
```

Pass Orwell CLI flags through `--app-args`:

```sh
python3 scripts/benchmark_layers.py --app-args "--disable-reconciliation"
```

Useful options:

- `--iterations N`: measured executions, default `10`.
- `--warmups N`: unrecorded warmup executions before measuring.
- `--gradle-args "--quiet"`: extra Gradle arguments.
- `--no-write`: print the table without writing reports.

Reports are written to `benchmarks/results/` as JSON and CSV. Reprint a saved
JSON report with:

```sh
python3 scripts/summarize_benchmark.py benchmarks/results/layer-benchmark-YYYYMMDD-HHMMSS.json
```
