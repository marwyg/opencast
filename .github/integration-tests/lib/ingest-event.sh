#!/bin/bash
#
# ingest-event.sh — Ingest a test event into Opencast and wait for processing.
#
# Usage:
#   .github/integration-tests/lib/ingest-event.sh <event_id> <event_title> [workflow_id]
#
# Arguments:
#   event_id      — Unique identifier for the event (used as mediapackage ID)
#   event_title   — Human-readable title for the event
#   workflow_id   — Workflow definition to use (default: "fast")
#
# Environment variables (required):
#   OPENCAST_URL          — Base URL of the Opencast instance
#   OPENCAST_ADMIN_USER   — Admin username
#   OPENCAST_ADMIN_PASS   — Admin password
#
# Environment variables (optional):
#   MEDIA_FILE            — Path to the media file to ingest
#   WORKFLOW_TIMEOUT      — Max seconds to wait for workflow completion (default: 600)
#
# Exit codes:
#   0 — Event ingested and workflow completed successfully
#   1 — Error during ingest or workflow failed
#

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")"/../../.. && pwd)"

# ---------------------------------------------------------------------------
# Arguments
# ---------------------------------------------------------------------------
EVENT_ID="${1:?Usage: ingest-event.sh <event_id> <event_title> [workflow_id]}"
EVENT_TITLE="${2:?Usage: ingest-event.sh <event_id> <event_title> [workflow_id]}"
WORKFLOW_ID="${3:-fast}"

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
OPENCAST_URL="${OPENCAST_URL:?OPENCAST_URL must be set}"
OPENCAST_ADMIN_USER="${OPENCAST_ADMIN_USER:?OPENCAST_ADMIN_USER must be set}"
OPENCAST_ADMIN_PASS="${OPENCAST_ADMIN_PASS:?OPENCAST_ADMIN_PASS must be set}"
MEDIA_FILE="${MEDIA_FILE:-${REPO_ROOT}/modules/inspection-service-ffmpeg/src/test/resources/segments_1.mp4}"
WORKFLOW_TIMEOUT="${WORKFLOW_TIMEOUT:-600}"

AUTH="${OPENCAST_ADMIN_USER}:${OPENCAST_ADMIN_PASS}"

# ---------------------------------------------------------------------------
# Step 1: Ingest the media package
# ---------------------------------------------------------------------------
ingest_event() {
  echo "::group::Ingesting event '${EVENT_TITLE}' (id=${EVENT_ID}, workflow=${WORKFLOW_ID})"

  if [ ! -f "${MEDIA_FILE}" ]; then
    echo "ERROR: Media file not found: ${MEDIA_FILE}"
    exit 1
  fi

  echo "Calling POST /ingest/addMediaPackage/${WORKFLOW_ID} ..."
  local http_code
  http_code=$(curl -s -o /dev/stderr -w "%{http_code}" \
    -u "${AUTH}" \
    "${OPENCAST_URL}/ingest/addMediaPackage/${WORKFLOW_ID}" \
    -F "flavor=presenter/source" \
    -F "BODY=@${MEDIA_FILE}" \
    -F "title=${EVENT_TITLE}" \
    -F "identifier=${EVENT_ID}" \
    -F "acl={\"acl\": {\"ace\": [{\"role\": \"ROLE_USER\",\"action\": \"read\"},{\"role\": \"ROLE_USER\",\"action\": \"write\"}]}}" \
    2>&1)

  if [[ "${http_code}" -lt 200 || "${http_code}" -ge 300 ]]; then
    echo "ERROR: Ingest returned HTTP ${http_code}"
    exit 1
  fi

  echo "Ingest successful (HTTP ${http_code})"
  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Step 2: Wait for the workflow to complete
# ---------------------------------------------------------------------------
wait_for_workflow() {
  echo "::group::Waiting for workflow to complete (timeout: ${WORKFLOW_TIMEOUT}s)"

  local status_url="${OPENCAST_URL}/workflow/mediaPackage/${EVENT_ID}/hasActiveWorkflows"
  local elapsed=0
  local poll_interval=5

  while true; do
    local active
    active=$(curl -s -u "${AUTH}" "${status_url}" || echo "error")

    if [ "${active}" = "false" ]; then
      echo ""
      echo "Workflow completed."
      break
    fi

    if [ "${elapsed}" -ge "${WORKFLOW_TIMEOUT}" ]; then
      echo ""
      echo "ERROR: Workflow did not complete within ${WORKFLOW_TIMEOUT} seconds"
      exit 1
    fi

    echo -n "."
    sleep "${poll_interval}"
    elapsed=$((elapsed + poll_interval))
  done

  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Step 3: Verify the workflow succeeded
# ---------------------------------------------------------------------------
verify_workflow() {
  echo "::group::Verifying workflow result for event '${EVENT_ID}'"

  local instances_url="${OPENCAST_URL}/workflow/mediaPackage/${EVENT_ID}/instances.json"
  local tmpfile
  tmpfile=$(mktemp)

  curl -s -u "${AUTH}" "${instances_url}" -o "${tmpfile}"

  echo "Workflow instances response:"
  jq . < "${tmpfile}" || cat "${tmpfile}"

  local state
  state=$(jq -r '.workflows.workflow.state' < "${tmpfile}" 2>/dev/null || echo "UNKNOWN")

  rm -f "${tmpfile}"

  if [ "${state}" != "SUCCEEDED" ]; then
    echo "ERROR: Workflow state is '${state}', expected 'SUCCEEDED'"
    exit 1
  fi

  echo "Workflow state: ${state} ✓"
  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
ingest_event
wait_for_workflow
verify_workflow

echo "Event '${EVENT_TITLE}' (id=${EVENT_ID}) created and processed successfully."
