#!/usr/bin/env bash
# SafeX Regression Test Runner
# Usage: ./scripts/run-regression.sh [label]
# label: "pre-merge" | "post-merge" | "pre-push" | "manual"

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$REPO_ROOT/test-logs"
HISTORY_FILE="$LOG_DIR/REGRESSION_HISTORY.md"

mkdir -p "$LOG_DIR"

# --- Capture context ---
LABEL="${1:-manual}"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H-%M-%SZ")
COMMIT=$(git -C "$REPO_ROOT" rev-parse --short HEAD 2>/dev/null || echo "unknown")
BRANCH=$(git -C "$REPO_ROOT" rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
COMMIT_MSG=$(git -C "$REPO_ROOT" log -1 --format="%s" 2>/dev/null || echo "unknown")

LOG_FILE="$LOG_DIR/regression-${TIMESTAMP}-${COMMIT}-${LABEL}.log"

echo "========================================"
echo "  SafeX Regression Test Suite"
echo "========================================"
echo "  Label   : $LABEL"
echo "  Commit  : $COMMIT  ($BRANCH)"
echo "  Message : $COMMIT_MSG"
echo "  Log     : test-logs/$(basename "$LOG_FILE")"
echo "========================================"

# --- Write log header ---
{
  echo "========================================"
  echo "SafeX Regression Test Run"
  echo "========================================"
  echo "Timestamp : $(date -u +"%Y-%m-%d %H:%M:%S UTC")"
  echo "Label     : $LABEL"
  echo "Commit    : $COMMIT"
  echo "Branch    : $BRANCH"
  echo "Message   : $COMMIT_MSG"
  echo "========================================"
  echo ""
} > "$LOG_FILE"

# --- Run Maven tests, tee to log ---
set +e
cd "$REPO_ROOT"
mvn test 2>&1 | tee -a "$LOG_FILE"
EXIT_CODE=${PIPESTATUS[0]}
set -e

# --- Parse summary ---
TOTAL=$(grep -E "Tests run:.*Time elapsed" "$LOG_FILE" | awk -F'[,:]' '{sum += $2} END {print sum+0}')
FAILURES=$(grep -E "Tests run:.*Time elapsed" "$LOG_FILE" | awk -F'[,:]' '{sum += $4} END {print sum+0}')
ERRORS=$(grep -E "Tests run:.*Time elapsed" "$LOG_FILE" | awk -F'[,:]' '{sum += $6} END {print sum+0}')

if [ "$EXIT_CODE" -eq 0 ]; then
  STATUS="PASS"
else
  STATUS="FAIL"
fi

# --- Append footer to log ---
{
  echo ""
  echo "========================================"
  echo "SUMMARY"
  echo "========================================"
  echo "Status   : $STATUS"
  echo "Total    : $TOTAL"
  echo "Failures : $FAILURES"
  echo "Errors   : $ERRORS"
  echo "========================================"
} >> "$LOG_FILE"

# --- Update REGRESSION_HISTORY.md ---
if [ ! -f "$HISTORY_FILE" ]; then
  {
    echo "# SafeX Regression Test History"
    echo ""
    echo "Auto-generated log of every regression run. Each entry links to its full log."
    echo ""
    echo "| Timestamp (UTC) | Label | Commit | Branch | Message | Tests | Failures | Status |"
    echo "|---|---|---|---|---|---|---|---|"
  } > "$HISTORY_FILE"
fi

echo "| $(date -u +"%Y-%m-%d %H:%M:%S") | $LABEL | \`$COMMIT\` | $BRANCH | $COMMIT_MSG | $TOTAL | $FAILURES | **$STATUS** |" >> "$HISTORY_FILE"

echo ""
echo "Result: $STATUS  ($TOTAL tests, $FAILURES failures, $ERRORS errors)"
echo "Full log saved: test-logs/$(basename "$LOG_FILE")"

exit $EXIT_CODE
