.PHONY: help up down reset seed test backend android lint

help:
	@echo "up       - start local stack (postgres, redis, backend)"
	@echo "down     - stop local stack"
	@echo "reset    - drop and recreate the database"
	@echo "seed     - load seed instruments and demo users"
	@echo "test     - run backend + android unit tests"
	@echo "backend  - run the backend only"
	@echo "android  - assemble the debug APK"

up:
	./scripts/start-local.sh

down:
	./scripts/stop-local.sh

reset:
	./scripts/reset-db.sh

seed:
	./scripts/seed-data.sh

test:
	./scripts/run-tests.sh

backend:
	cd backend && ./gradlew run

android:
	cd android-app && ./gradlew assembleDebug

lint:
	cd backend && ./gradlew ktlintCheck
	cd android-app && ./gradlew ktlintCheck
