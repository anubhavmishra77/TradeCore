#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Starting TradeCore local stack..."
docker compose up -d postgres redis
echo "Waiting for postgres..."
until docker compose exec -T postgres pg_isready -U tradecore >/dev/null 2>&1; do sleep 1; done
docker compose up -d backend
echo "Backend on http://localhost:8080"
