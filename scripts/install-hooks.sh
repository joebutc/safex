#!/usr/bin/env bash
# Install SafeX git hooks for automatic regression testing
# Run once: ./scripts/install-hooks.sh

set -euo pipefail
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HOOKS_DIR="$REPO_ROOT/.git/hooks"

echo "Installing SafeX regression git hooks..."

# --- post-merge hook ---
cat > "$HOOKS_DIR/post-merge" << 'HOOK'
#!/usr/bin/env bash
# Runs regression tests automatically after every git merge
REPO_ROOT="$(git rev-parse --show-toplevel)"
echo ""
echo "[SafeX Hook] Running post-merge regression tests..."
bash "$REPO_ROOT/scripts/run-regression.sh" "post-merge"
HOOK
chmod +x "$HOOKS_DIR/post-merge"
echo "  [OK] post-merge hook installed"

# --- pre-push hook ---
cat > "$HOOKS_DIR/pre-push" << 'HOOK'
#!/usr/bin/env bash
# Blocks push if regression tests fail
REPO_ROOT="$(git rev-parse --show-toplevel)"
echo ""
echo "[SafeX Hook] Running pre-push regression tests..."
bash "$REPO_ROOT/scripts/run-regression.sh" "pre-push"
EXIT=$?
if [ $EXIT -ne 0 ]; then
  echo ""
  echo "[SafeX Hook] PUSH BLOCKED: regression tests failed."
  echo "  Fix the failures before pushing."
  exit 1
fi
HOOK
chmod +x "$HOOKS_DIR/pre-push"
echo "  [OK] pre-push hook installed"

echo ""
echo "Done. Hooks active:"
echo "  post-merge -> runs regression after every merge (logs + updates REGRESSION_HISTORY.md)"
echo "  pre-push   -> blocks push if any test fails"
