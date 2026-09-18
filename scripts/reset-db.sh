#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Resetting database (all data will be lost)..."
docker compose down -v postgres
docker compose up -d postgres
until docker compose exec -T postgres pg_isready -U tradecore >/dev/null 2>&1; do sleep 1; done
cd backend && ./gradlew flywayMigrate
echo "Database reset."
