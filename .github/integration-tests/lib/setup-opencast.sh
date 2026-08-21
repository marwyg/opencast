#!/bin/bash
#
# setup-opencast.sh — Build, configure, and start an Opencast allinone instance
#                      with PostgreSQL + OpenSearch via Docker Compose.
#
# Usage:
#   source .github/integration-tests/lib/setup-opencast.sh
#
# Exported variables after sourcing:
#   OPENCAST_URL          — Base URL of the running Opencast instance
#   OPENCAST_HOME         — Root directory of the extracted Opencast distribution
#   OPENCAST_STORAGE_DIR  — Opencast's storage directory (karaf.data/opencast)
#   OPENCAST_ADMIN_USER   — Admin username
#   OPENCAST_ADMIN_PASS   — Admin password
#
# Prerequisites:
#   - Java 21+, Maven wrapper (./mvnw), Docker + Docker Compose
#   - System packages: ffmpeg, tesseract-ocr, etc. (see workflow)
#

set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration (can be overridden before sourcing)
# ---------------------------------------------------------------------------
OPENCAST_URL="${OPENCAST_URL:-http://localhost:8080}"
OPENCAST_ADMIN_USER="${OPENCAST_ADMIN_USER:-admin}"
OPENCAST_ADMIN_PASS="${OPENCAST_ADMIN_PASS:-opencast}"
OPENCAST_DB_DRIVER="${OPENCAST_DB_DRIVER:-org.mariadb.jdbc.Driver}"
OPENCAST_DB_URL="${OPENCAST_DB_URL:-jdbc:mariadb://localhost/opencast?useMysqlMetadata=true}"
OPENCAST_DB_USER="${OPENCAST_DB_USER:-opencast}"
OPENCAST_DB_PASS="${OPENCAST_DB_PASS:-dbpassword}"
SKIP_BUILD="${SKIP_BUILD:-false}"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")"/../../.. && pwd)"

# ---------------------------------------------------------------------------
# Step 1: Build Opencast (unless SKIP_BUILD=true)
# ---------------------------------------------------------------------------
setup_build_opencast() {
  if [ "${SKIP_BUILD}" = "true" ]; then
    echo "::group::Skipping build (SKIP_BUILD=true)"
    echo "::endgroup::"
    return 0
  fi

  echo "::group::Building Opencast"
  cd "${REPO_ROOT}"
  ./mvnw clean install -Pnone \
    --batch-mode \
    -Dsurefire.rerunFailingTestsCount=2 \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

  echo "Building assemblies..."
  cd "${REPO_ROOT}/assemblies"
  ../mvnw clean install \
    --batch-mode \
    -Dsurefire.rerunFailingTestsCount=2 \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120
  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Step 2: Extract the allinone distribution
# ---------------------------------------------------------------------------
setup_extract_distribution() {
  echo "::group::Extracting Opencast allinone distribution"
  cd "${REPO_ROOT}"

  local build_dir="${REPO_ROOT}/build"
  mkdir -p "${build_dir}"
  cd "${build_dir}"

  # Clean up any previous extraction
  find . -maxdepth 1 -type d -name 'opencast-dist-allinone*' -exec rm -rf {} + 2>/dev/null || true

  local tarball
  tarball=$(find "${REPO_ROOT}/assemblies/dist-allinone/target/" -name 'opencast-dist-allinone*.tar.gz' | head -1)
  if [ -z "${tarball}" ]; then
    echo "ERROR: Could not find allinone tarball in assemblies/dist-allinone/target/"
    exit 1
  fi

  echo "Extracting ${tarball}..."
  tar xf "${tarball}"

  # Find the extracted directory
  OPENCAST_HOME=$(find . -maxdepth 1 -type d -name 'opencast-dist-allinone*' | head -1)
  OPENCAST_HOME="$(cd "${OPENCAST_HOME}" && pwd)"

  export OPENCAST_HOME
  export OPENCAST_STORAGE_DIR="${OPENCAST_HOME}/data/opencast"
  echo "OPENCAST_HOME=${OPENCAST_HOME}"
  echo "OPENCAST_STORAGE_DIR=${OPENCAST_STORAGE_DIR}"
  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Step 3: Start Docker Compose services (OpenSearch + MariaDB)
# ---------------------------------------------------------------------------
setup_start_docker_services() {
  echo "::group::Starting Docker Compose services (OpenSearch + MariaDB)"
  cd "${REPO_ROOT}/docs/scripts/devel-dependency-containers/"
  docker compose -f docker-compose-all-sql.yml down 2>/dev/null || true
  docker compose -f docker-compose-all-sql.yml up -d opensearch mariadb

  echo "Waiting for OpenSearch..."
  curl -fisS --retry 60 --retry-delay 1 --retry-all-errors http://localhost:9200/

  echo "Waiting for MariaDB..."
  for i in $(seq 1 30); do
    if docker compose -f docker-compose-all-sql.yml exec -T mariadb mariadb-admin ping -u opencast -pdbpassword 2>/dev/null; then
      break
    fi
    echo "  Attempt ${i}/30..."
    sleep 2
  done

  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Step 4: Configure Opencast for MariaDB
# ---------------------------------------------------------------------------
setup_configure_opencast() {
  echo "::group::Configuring Opencast"
  local props="${OPENCAST_HOME}/etc/custom.properties"

  # Database configuration
  sed -i "s|^.*org.opencastproject.db.jdbc.driver=.*$|org.opencastproject.db.jdbc.driver=${OPENCAST_DB_DRIVER}|" "${props}"
  sed -i "s|^.*org.opencastproject.db.jdbc.url=.*$|org.opencastproject.db.jdbc.url=${OPENCAST_DB_URL}|" "${props}"
  sed -i "s|^.*org.opencastproject.db.jdbc.user=.*$|org.opencastproject.db.jdbc.user=${OPENCAST_DB_USER}|" "${props}"
  sed -i "s|^.*org.opencastproject.db.jdbc.pass=.*$|org.opencastproject.db.jdbc.pass=${OPENCAST_DB_PASS}|" "${props}"

  echo "Database configured: ${OPENCAST_DB_DRIVER} -> ${OPENCAST_DB_URL}"
  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Step 5: Start Opencast
# ---------------------------------------------------------------------------
setup_start_opencast() {
  echo "::group::Starting Opencast"
  "${OPENCAST_HOME}/bin/start-opencast" daemon &
  OPENCAST_PID=$!
  export OPENCAST_PID
  echo "Opencast started with PID ${OPENCAST_PID}"
  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Step 6: Wait for Opencast to become healthy
# ---------------------------------------------------------------------------
setup_wait_for_healthy() {
  echo "::group::Waiting for Opencast to become healthy"
  echo "Polling ${OPENCAST_URL}/sysinfo/bundles/version ..."
  curl -fisS --retry 60 --retry-delay 10 --retry-all-errors \
    -u "${OPENCAST_ADMIN_USER}:${OPENCAST_ADMIN_PASS}" \
    "${OPENCAST_URL}/sysinfo/bundles/version"
  echo ""
  echo "Opencast is healthy!"
  echo "::endgroup::"
}

# ---------------------------------------------------------------------------
# Main: Run all setup steps in order
# ---------------------------------------------------------------------------
setup_opencast() {
  setup_build_opencast
  setup_extract_distribution
  setup_start_docker_services
  setup_configure_opencast
  setup_start_opencast
  setup_wait_for_healthy

  echo "============================================"
  echo "Opencast is running at ${OPENCAST_URL}"
  echo "  OPENCAST_HOME=${OPENCAST_HOME}"
  echo "  OPENCAST_STORAGE_DIR=${OPENCAST_STORAGE_DIR}"
  echo "============================================"
}

# Export all variables and functions for downstream scripts
export OPENCAST_URL OPENCAST_ADMIN_USER OPENCAST_ADMIN_PASS
export -f setup_build_opencast setup_extract_distribution setup_start_docker_services
export -f setup_configure_opencast setup_start_opencast setup_wait_for_healthy
export -f setup_opencast
