#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "== Backend tests =="
(cd backend && ./gradlew test)

echo "== Android unit tests =="
(cd android-app && ./gradlew testDebugUnitTest)
