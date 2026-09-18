#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Stopping TradeCore local stack..."
docker compose down
