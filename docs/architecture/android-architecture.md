# Android Architecture

## Layers
Clean Architecture per feature module: `data` → `domain` → `presentation`.
Only `domain` is dependency-free; `presentation` never touches `data` directly.

## Modules
- `core:common` — Result type, errors, dispatchers
- `core:model` — shared domain models
- `core:network` — Retrofit/OkHttp client, interceptors, WebSocket manager
- `core:database` — Room entities, DAOs, mappers
- `core:datastore` — token and preference storage
- `core:ui` — design system and shared composables
- `feature:*` — self-contained vertical slices

## State
Each screen has a `ViewModel` exposing a single immutable `UiState` via `StateFlow`,
and consumes `UiEvent` from the UI. Side effects go through a `Channel`.

## Offline
Room is the single source of truth for instruments, watchlists and order history.
The repository emits cached data first, then refreshes from the network.
See ADR-004.
