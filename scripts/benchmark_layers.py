#!/usr/bin/env python3
"""Benchmark Orwell pipeline layers by parsing the Java timing summary.

The Java application already reports layer timings in the form:

    === Execution Summary ===
    Extraction                       123 ms
    ...
    Total                            456 ms

This script runs the application several times, parses those summaries, and
prints average timing statistics per layer.
"""

from __future__ import annotations

import argparse
import csv
import json
import re
import shlex
import statistics
import subprocess
import sys
import time
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable


REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_RESULTS_DIR = REPO_ROOT / "benchmarks" / "results"
SUMMARY_HEADER = "=== Execution Summary ==="
TIMING_LINE = re.compile(r"^(?P<name>.+?)\s+(?P<millis>\d+)\s+ms$")


@dataclass(frozen=True)
class RunResult:
    iteration: int
    wall_clock_ms: int
    timings_ms: dict[str, int]


def parse_timing_summary(output: str) -> dict[str, int]:
    """Parse layer timings from the application's benchmark summary."""
    lines = output.splitlines()
    try:
        start = lines.index(SUMMARY_HEADER) + 1
    except ValueError as exc:
        raise ValueError(f"could not find '{SUMMARY_HEADER}' in process output") from exc

    timings: dict[str, int] = {}
    for line in lines[start:]:
        stripped = line.strip()
        if not stripped or set(stripped) == {"-"}:
            continue

        match = TIMING_LINE.match(line)
        if match is None:
            continue

        timings[match.group("name").strip()] = int(match.group("millis"))

    if not timings:
        raise ValueError("found benchmark summary header, but no timing rows")

    return timings


def build_command(gradle_args: str, app_args: str) -> list[str]:
    command = ["./gradlew", "run"]
    if gradle_args:
        command.extend(shlex.split(gradle_args))
    if app_args:
        command.append(f"--args={app_args}")
    return command


def run_once(command: list[str], iteration: int) -> RunResult:
    start = time.perf_counter()
    completed = subprocess.run(
        command,
        cwd=REPO_ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        check=False,
    )
    wall_clock_ms = round((time.perf_counter() - start) * 1000)

    if completed.returncode != 0:
        raise RuntimeError(
            f"iteration {iteration} failed with exit code {completed.returncode}\n"
            f"{completed.stdout}"
        )

    timings = parse_timing_summary(completed.stdout)
    timings["Process Wall Clock"] = wall_clock_ms
    return RunResult(iteration=iteration, wall_clock_ms=wall_clock_ms, timings_ms=timings)


def stats(values: list[int]) -> dict[str, float | int]:
    return {
        "runs": len(values),
        "avg_ms": round(statistics.fmean(values), 2),
        "median_ms": round(statistics.median(values), 2),
        "min_ms": min(values),
        "max_ms": max(values),
        "stdev_ms": round(statistics.stdev(values), 2) if len(values) > 1 else 0.0,
    }


def summarize(results: Iterable[RunResult]) -> dict[str, dict[str, float | int]]:
    by_layer: dict[str, list[int]] = {}
    for result in results:
        for layer, millis in result.timings_ms.items():
            by_layer.setdefault(layer, []).append(millis)

    return {layer: stats(values) for layer, values in by_layer.items()}


def print_table(summary: dict[str, dict[str, float | int]]) -> None:
    print(f"{'Layer':30} {'Runs':>4} {'Avg ms':>10} {'Median':>10} {'Min':>8} {'Max':>8} {'Stdev':>10}")
    print("-" * 88)
    for layer, row in summary.items():
        print(
            f"{layer:30} "
            f"{row['runs']:>4} "
            f"{row['avg_ms']:>10} "
            f"{row['median_ms']:>10} "
            f"{row['min_ms']:>8} "
            f"{row['max_ms']:>8} "
            f"{row['stdev_ms']:>10}"
        )


def write_csv(path: Path, summary: dict[str, dict[str, float | int]]) -> None:
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(
            handle,
            fieldnames=["layer", "runs", "avg_ms", "median_ms", "min_ms", "max_ms", "stdev_ms"],
        )
        writer.writeheader()
        for layer, row in summary.items():
            writer.writerow({"layer": layer, **row})


def write_json(
    path: Path,
    command: list[str],
    app_args: str,
    results: list[RunResult],
    summary: dict[str, dict[str, float | int]],
) -> None:
    payload = {
        "created_at": datetime.now(timezone.utc).isoformat(),
        "command": command,
        "app_args": app_args,
        "runs": [
            {
                "iteration": result.iteration,
                "wall_clock_ms": result.wall_clock_ms,
                "timings_ms": result.timings_ms,
            }
            for result in results
        ],
        "summary": summary,
    }
    path.write_text(json.dumps(payload, indent=2), encoding="utf-8")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Run Orwell several times and average per-layer execution timings."
    )
    parser.add_argument(
        "-n",
        "--iterations",
        type=int,
        default=10,
        help="number of measured executions to run (default: 10)",
    )
    parser.add_argument(
        "--warmups",
        type=int,
        default=0,
        help="number of unrecorded warmup executions before measuring",
    )
    parser.add_argument(
        "--app-args",
        default="",
        help='arguments passed to the Java app, for example "--disable-reconciliation"',
    )
    parser.add_argument(
        "--gradle-args",
        default="",
        help='extra arguments passed to Gradle before --args, for example "--quiet"',
    )
    parser.add_argument(
        "--results-dir",
        type=Path,
        default=DEFAULT_RESULTS_DIR,
        help=f"directory where JSON and CSV reports are written (default: {DEFAULT_RESULTS_DIR})",
    )
    parser.add_argument(
        "--no-write",
        action="store_true",
        help="print results only; do not write JSON or CSV reports",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if args.iterations < 1:
        print("--iterations must be at least 1", file=sys.stderr)
        return 2
    if args.warmups < 0:
        print("--warmups cannot be negative", file=sys.stderr)
        return 2

    command = build_command(args.gradle_args, args.app_args)
    print("Command:", " ".join(command))

    for warmup in range(1, args.warmups + 1):
        print(f"Warmup {warmup}/{args.warmups}")
        run_once(command, warmup)

    results: list[RunResult] = []
    for iteration in range(1, args.iterations + 1):
        print(f"Iteration {iteration}/{args.iterations}")
        results.append(run_once(command, iteration))

    summary = summarize(results)
    print()
    print_table(summary)

    if not args.no_write:
        args.results_dir.mkdir(parents=True, exist_ok=True)
        stem = datetime.now().strftime("layer-benchmark-%Y%m%d-%H%M%S")
        json_path = args.results_dir / f"{stem}.json"
        csv_path = args.results_dir / f"{stem}.csv"
        write_json(json_path, command, args.app_args, results, summary)
        write_csv(csv_path, summary)
        print()
        print(f"Wrote {json_path}")
        print(f"Wrote {csv_path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
