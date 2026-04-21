#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$REPO_ROOT/test-logs"
HISTORY_FILE="$LOG_DIR/REGRESSION_HISTORY.md"
PERSIST_LOGS="${REGRESSION_PERSIST_LOGS:-1}"
STAGE_LOGS="${REGRESSION_STAGE_LOGS:-0}"
TMP_LOG_DIR=""

mkdir -p "$LOG_DIR"

LABEL="${1:-manual}"
TIMESTAMP="$(date -u +"%Y-%m-%dT%H-%M-%SZ")"
COMMIT="$(git -C "$REPO_ROOT" rev-parse --short HEAD 2>/dev/null || echo "unknown")"
BRANCH="$(git -C "$REPO_ROOT" rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")"
COMMIT_MSG="$(git -C "$REPO_ROOT" log -1 --format="%s" 2>/dev/null || echo "unknown")"

DIRTY_SUFFIX=""
if [ -n "$(git -C "$REPO_ROOT" status --porcelain 2>/dev/null)" ]; then
  DIRTY_SUFFIX="-dirty"
fi

COMMIT_LABEL="${COMMIT}${DIRTY_SUFFIX}"
if [ "$PERSIST_LOGS" = "1" ]; then
  LOG_FILE="$LOG_DIR/regression-${TIMESTAMP}-${COMMIT_LABEL}-${LABEL}.log"
else
  TMP_LOG_DIR="$(mktemp -d "${TMPDIR:-/tmp}/safex-regression-XXXXXX")"
  LOG_FILE="$TMP_LOG_DIR/regression-${TIMESTAMP}-${COMMIT_LABEL}-${LABEL}.log"
fi

cleanup_temp_logs() {
  if [ "$PERSIST_LOGS" != "1" ] && [ -n "$TMP_LOG_DIR" ] && [ -d "$TMP_LOG_DIR" ]; then
    rm -rf "$TMP_LOG_DIR"
  fi
}

trap cleanup_temp_logs EXIT

echo "========================================"
echo "  SafeX Regression Test Suite"
echo "========================================"
echo "  Label   : $LABEL"
echo "  Commit  : $COMMIT_LABEL  ($BRANCH)"
echo "  Message : $COMMIT_MSG"
if [ "$PERSIST_LOGS" = "1" ]; then
  echo "  Log     : test-logs/$(basename "$LOG_FILE")"
else
  echo "  Log     : temporary (not persisted to the repository)"
fi
echo "========================================"

{
  echo "========================================"
  echo "SafeX Regression Test Run"
  echo "========================================"
  echo "Timestamp : $(date -u +"%Y-%m-%d %H:%M:%S UTC")"
  echo "Label     : $LABEL"
  echo "Commit    : $COMMIT_LABEL"
  echo "Branch    : $BRANCH"
  echo "Message   : $COMMIT_MSG"
  echo "========================================"
  echo ""
} > "$LOG_FILE"

set +e
cd "$REPO_ROOT"
mvn -B -ntp test 2>&1 | tee -a "$LOG_FILE"
EXIT_CODE=${PIPESTATUS[0]}
set -e

SUMMARY_LINE="$(grep -E '^\[INFO\] Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+' "$LOG_FILE" | tail -n 1 || true)"
if [ -n "$SUMMARY_LINE" ]; then
  TOTAL="$(echo "$SUMMARY_LINE" | awk -F'[,:]' '{print $2+0}')"
  FAILURES="$(echo "$SUMMARY_LINE" | awk -F'[,:]' '{print $4+0}')"
  ERRORS="$(echo "$SUMMARY_LINE" | awk -F'[,:]' '{print $6+0}')"
else
  TOTAL=0
  FAILURES=0
  ERRORS=0
fi

if [ "$EXIT_CODE" -eq 0 ]; then
  STATUS="PASS"
else
  STATUS="FAIL"
fi

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

if [ "$PERSIST_LOGS" = "1" ]; then
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

  echo "| $(date -u +"%Y-%m-%d %H:%M:%S") | $LABEL | \`$COMMIT_LABEL\` | $BRANCH | $COMMIT_MSG | $TOTAL | $FAILURES | **$STATUS** |" >> "$HISTORY_FILE"

  if [ "$STAGE_LOGS" = "1" ]; then
    git -C "$REPO_ROOT" add -- "$HISTORY_FILE" "$LOG_FILE"
    echo "Staged: test-logs/REGRESSION_HISTORY.md and test-logs/$(basename "$LOG_FILE")"
  fi
fi

echo ""
echo "Result: $STATUS  ($TOTAL tests, $FAILURES failures, $ERRORS errors)"
if [ "$PERSIST_LOGS" = "1" ]; then
  echo "Full log saved: test-logs/$(basename "$LOG_FILE")"
else
  echo "Full log captured in a temporary file for this run only."
fi

exit "$EXIT_CODE"
