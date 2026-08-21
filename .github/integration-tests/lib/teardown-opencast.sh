#!/bin/bash
#
# teardown-opencast.sh — Stop Opencast and Docker Compose services, collect logs.
#
# Usage:
#   .github/integration-tests/lib/teardown-opencast.sh
#
# Environment variables (optional):
#   OPENCAST_HOME   — Root directory of the Opencast distribution
#   OPENCAST_PID    — PID of the Opencast process (if started with daemon &)
#
# This script never fails (set +e) so it can always run in cleanup.
#

set +e  # Don't fail on errors during teardown

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")"/../../.. && pwd)"

# ---------------------------------------------------------------------------
# Step 1: Stop Opencast
# ---------------------------------------------------------------------------
echo "::group::Stopping Opencast"

if [ -n "${OPENCAST_HOME:-}" ] && [ -x "${OPENCAST_HOME}/bin/stop-opencast" ]; then
  echo "Stopping Opencast via stop-opencast script..."
  "${OPENCAST_HOME}/bin/stop-opencast" || true
elif [ -n "${OPENCAST_PID:-}" ]; then
  echo "Killing Opencast process (PID ${OPENCAST_PID})..."
  kill "${OPENCAST_PID}" 2>/dev/null || true
  wait "${OPENCAST_PID}" 2>/dev/null || true
fi

echo "::endgroup::"

# ---------------------------------------------------------------------------
# Step 2: Collect Opencast logs
# ---------------------------------------------------------------------------
echo "::group::Opencast logs"

if [ -n "${OPENCAST_HOME:-}" ]; then
  local_log="${OPENCAST_HOME}/data/log/opencast.log"
  if [ -f "${local_log}" ]; then
    echo "--- ${local_log} ---"
    cat "${local_log}"
  else
    echo "No log file found at ${local_log}"
  fi
else
  echo "OPENCAST_HOME not set, skipping log collection"
fi

echo "::endgroup::"

# ---------------------------------------------------------------------------
# Step 3: Stop Docker Compose services
# ---------------------------------------------------------------------------
echo "::group::Stopping Docker Compose services"

cd "${REPO_ROOT}/docs/scripts/devel-dependency-containers/" 2>/dev/null || true
docker compose -f docker-compose-all-sql.yml down 2>/dev/null || true

echo "::endgroup::"

echo "Teardown complete."
