# TradeCore

A simulated trading platform: a modular Android client (Kotlin + Compose) and a
Kotlin/Ktor backend with real order lifecycle, execution, positions and wallet accounting.

## Stack

**Android** — Kotlin, Jetpack Compose, Hilt, Room, Retrofit/OkHttp, DataStore, Coroutines/Flow
**Backend** — Ktor, Exposed, PostgreSQL, Redis, Flyway, JWT auth, WebSockets
**Infra** — Docker Compose, Prometheus + Grafana, GitHub Actions

## Getting started

One-time setup — the Gradle wrapper JAR can't be committed here, so generate it
(requires Gradle 8.7+ installed locally, or just open the project in Android Studio,
which does this for you):

```bash
cd android-app && gradle wrapper
cd ../backend  && gradle wrapper
```

Then:

```bash
# start postgres, redis and the backend
make up

# load demo instruments and accounts
make seed

# run everything's tests
make test
```

The backend listens on `http://localhost:8080`. From the Android emulator, reach it at
`http://10.0.2.2:8080` (already configured in the debug build type).

Open `android-app/` in Android Studio and run the `app` configuration.

## Layout

```
android-app/   modular Compose client (core:* + feature:*)
backend/       Ktor service, one package per bounded context
docs/          architecture notes, API reference, ADRs, diagrams
infra/         Dockerfiles, database bootstrap, monitoring config
scripts/       local dev helpers
```

## Where to read first

- `docs/architecture/system-design.md` — how the pieces fit together
- `docs/decisions/` — why things are the way they are (ADRs)
- `docs/api/` — endpoint reference

## Status

Scaffolded. Structure, Gradle configuration, migrations, resources and documentation
are in place; Kotlin sources are stubs marked with `TODO` and ready to be filled in.

Dependency versions live in `android-app/gradle/libs.versions.toml` — change them
there, not in individual module files.
