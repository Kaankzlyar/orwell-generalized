#!/usr/bin/env python3
"""Print a table from a JSON report created by benchmark_layers.py."""

from __future__ import annotations

import argparse
import json
from pathlib import Path


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


def main() -> int:
    parser = argparse.ArgumentParser(description="Summarize an Orwell benchmark JSON report.")
    parser.add_argument("report", type=Path, help="path to a JSON report from benchmark_layers.py")
    args = parser.parse_args()

    payload = json.loads(args.report.read_text(encoding="utf-8"))
    print_table(payload["summary"])
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
