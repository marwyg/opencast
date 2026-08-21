#!/bin/bash
#
# test-corrupted-assetmanager-deletion.sh
#
# Integration test: Create an event, publish it, then delete the asset manager
# directory from the filesystem behind Opencast's back. Assert that the delete
# API handles this corruption gracefully (returns a proper response, doesn't hang).
#
# Usage:
#   .github/integration-tests/test-corrupted-assetmanager-deletion.sh
#
# This script handles its own setup and teardown.
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIB_DIR="${SCRIPT_DIR}/lib"

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
EVENT_ID="test-corrupted-am-$(date +%s)"
EVENT_TITLE="Test Corrupted AssetManager Deletion"
DELETE_TIMEOUT=120  # seconds to wait for the delete API call

# Track test result
TEST_PASSED=false

# ---------------------------------------------------------------------------
# Cleanup trap — always teardown
# ---------------------------------------------------------------------------
cleanup() {
  echo ""
  echo "============================================"
  echo "TEARDOWN"
  echo "============================================"
  bash "${LIB_DIR}/teardown-opencast.sh"

  echo ""
  if [ "${TEST_PASSED}" = "true" ]; then
    echo "✅ TEST PASSED"
  else
    echo "❌ TEST FAILED"
    exit 1
  fi
}
trap cleanup EXIT

# ---------------------------------------------------------------------------
# Step 1: Setup Opencast
# ---------------------------------------------------------------------------
echo "============================================"
echo "STEP 1: Setting up Opencast"
echo "============================================"

# shellcheck source=lib/setup-opencast.sh
source "${LIB_DIR}/setup-opencast.sh"
setup_opencast

# ---------------------------------------------------------------------------
# Step 2: Ingest and process a test event
# ---------------------------------------------------------------------------
echo ""
echo "============================================"
echo "STEP 2: Creating test event"
echo "============================================"

bash "${LIB_DIR}/ingest-event.sh" "${EVENT_ID}" "${EVENT_TITLE}"

# ---------------------------------------------------------------------------
# Step 3: Verify the event exists in the asset manager filesystem
# ---------------------------------------------------------------------------
echo ""
echo "============================================"
echo "STEP 3: Verifying asset manager state"
echo "============================================"

ARCHIVE_DIR="${OPENCAST_STORAGE_DIR}/archive/mh_default_org"

echo "Looking for event directory in ${ARCHIVE_DIR}..."

# The asset manager stores files under archive/mh_default_org/<event-id>/
EVENT_ARCHIVE_DIR="${ARCHIVE_DIR}/${EVENT_ID}"

if [ -d "${EVENT_ARCHIVE_DIR}" ]; then
  echo "Found event archive directory: ${EVENT_ARCHIVE_DIR}"
  echo "Contents:"
  find "${EVENT_ARCHIVE_DIR}" -type f | head -20
else
  echo "WARNING: Event archive directory not found at expected path."
  echo "Searching for event ID in archive..."
  find "${ARCHIVE_DIR}" -name "*${EVENT_ID}*" -o -type d | head -20 || true

  # Try to find it by listing what's in the archive
  echo ""
  echo "Archive directory contents:"
  ls -la "${ARCHIVE_DIR}/" 2>/dev/null || echo "Archive dir does not exist"

  # If the archive dir structure is different, try a broader search
  EVENT_ARCHIVE_DIR=$(find "${OPENCAST_STORAGE_DIR}" -type d -name "${EVENT_ID}" 2>/dev/null | head -1)
  if [ -z "${EVENT_ARCHIVE_DIR}" ]; then
    echo "ERROR: Could not find event archive directory anywhere under ${OPENCAST_STORAGE_DIR}"
    echo "Full storage tree:"
    find "${OPENCAST_STORAGE_DIR}" -maxdepth 4 -type d 2>/dev/null || true
    exit 1
  fi
  echo "Found event at: ${EVENT_ARCHIVE_DIR}"
fi

# ---------------------------------------------------------------------------
# Step 4: Verify the event exists via the API
# ---------------------------------------------------------------------------
echo ""
echo "::group::Verifying event exists via API"

API_CHECK_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -u "${OPENCAST_ADMIN_USER}:${OPENCAST_ADMIN_PASS}" \
  "${OPENCAST_URL}/api/events/${EVENT_ID}")

if [ "${API_CHECK_CODE}" != "200" ]; then
  echo "ERROR: Event not found via API (HTTP ${API_CHECK_CODE})"
  exit 1
fi
echo "Event exists in API ✓ (HTTP ${API_CHECK_CODE})"
echo "::endgroup::"

# ---------------------------------------------------------------------------
# Step 5: INJECT FAULT — Delete the asset manager directory
# ---------------------------------------------------------------------------
echo ""
echo "============================================"
echo "STEP 5: INJECTING FAULT"
echo "  Deleting asset manager directory:"
echo "  ${EVENT_ARCHIVE_DIR}"
echo "============================================"

rm -rf "${EVENT_ARCHIVE_DIR}"

if [ -d "${EVENT_ARCHIVE_DIR}" ]; then
  echo "ERROR: Failed to delete asset manager directory"
  exit 1
fi
echo "Asset manager directory deleted ✓"

# Verify the directory is gone
echo "Confirming directory no longer exists..."
if [ ! -d "${EVENT_ARCHIVE_DIR}" ]; then
  echo "Confirmed: directory removed ✓"
fi

# ---------------------------------------------------------------------------
# Step 6: Attempt to delete the corrupted event via the API
# ---------------------------------------------------------------------------
echo ""
echo "============================================"
echo "STEP 6: Deleting corrupted event via API"
echo "  DELETE ${OPENCAST_URL}/api/events/${EVENT_ID}"
echo "  Timeout: ${DELETE_TIMEOUT}s"
echo "============================================"

DELETE_RESPONSE_FILE=$(mktemp)

# Use timeout to ensure the API call doesn't hang
set +e
DELETE_HTTP_CODE=$(timeout "${DELETE_TIMEOUT}" \
  curl -s -o "${DELETE_RESPONSE_FILE}" -w "%{http_code}" \
    -X DELETE \
    -u "${OPENCAST_ADMIN_USER}:${OPENCAST_ADMIN_PASS}" \
    "${OPENCAST_URL}/api/events/${EVENT_ID}")
DELETE_EXIT_CODE=$?
set -e

echo "Exit code: ${DELETE_EXIT_CODE}"
echo "HTTP status: ${DELETE_HTTP_CODE:-N/A}"

if [ -f "${DELETE_RESPONSE_FILE}" ]; then
  echo "Response body:"
  cat "${DELETE_RESPONSE_FILE}" 2>/dev/null || true
  echo ""
  rm -f "${DELETE_RESPONSE_FILE}"
fi

# ---------------------------------------------------------------------------
# Step 7: Assert the result
# ---------------------------------------------------------------------------
echo ""
echo "============================================"
echo "STEP 7: Asserting result"
echo "============================================"

# Exit code 124 from timeout means the command timed out
if [ "${DELETE_EXIT_CODE}" -eq 124 ]; then
  echo "FAIL: DELETE request timed out after ${DELETE_TIMEOUT}s — Opencast is hanging!"
  exit 1
fi

# If curl itself failed (not timeout)
if [ "${DELETE_EXIT_CODE}" -ne 0 ]; then
  echo "FAIL: curl failed with exit code ${DELETE_EXIT_CODE}"
  exit 1
fi

# Assert acceptable HTTP status codes:
#   202 — Accepted (retraction workflow started — even if it may fail later, the API responded)
#   204 — No Content (event deleted successfully despite missing files)
#   404 — Not Found (Opencast detected the event is gone — acceptable)
#   500 — Server Error (expected error — acceptable as long as it didn't hang)
case "${DELETE_HTTP_CODE}" in
  202)
    echo "PASS: API returned 202 Accepted (retraction workflow started)"
    echo "  The API responded promptly. The retraction workflow was started."
    echo "  (The workflow may fail due to missing files, but the API didn't hang.)"
    TEST_PASSED=true
    ;;
  204)
    echo "PASS: API returned 204 No Content (event deleted)"
    echo "  Opencast successfully cleaned up the corrupted event."
    TEST_PASSED=true
    ;;
  404)
    echo "PASS: API returned 404 Not Found"
    echo "  Opencast detected the event data is missing and reported not found."
    TEST_PASSED=true
    ;;
  500)
    echo "PASS (with warning): API returned 500 Internal Server Error"
    echo "  This is acceptable — the API returned a proper error instead of hanging."
    echo "  However, a graceful 404 or 204 would be preferable."
    TEST_PASSED=true
    ;;
  *)
    echo "FAIL: Unexpected HTTP status code: ${DELETE_HTTP_CODE}"
    echo "  Expected one of: 202, 204, 404, 500"
    exit 1
    ;;
esac

echo ""
echo "============================================"
echo "Test completed successfully."
echo "============================================"
