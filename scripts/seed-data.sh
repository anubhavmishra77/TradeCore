#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Seeding instruments and demo accounts..."
cd backend && ./gradlew run --args="--seed"
