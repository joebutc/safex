#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

echo "[SafeX Regression] Running Maven test suite..."
mvn -B -ntp test

echo "[SafeX Regression] Completed successfully."
