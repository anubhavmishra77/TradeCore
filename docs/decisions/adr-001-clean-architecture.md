# ADR-001: Clean Architecture with feature modules

- Status: Accepted
- Date: 2026-01-10

## Context
The Android app has several independent surfaces (auth, market, orders, portfolio, wallet)
and we expect more than one person working in it at a time. A single `app` module would
make build times and merge conflicts grow together.

## Decision
Split into `core:*` and `feature:*` Gradle modules. Inside each feature, use
`data` / `domain` / `presentation` with dependencies pointing inward only. Repository
interfaces live in `domain`; implementations live in `data`.

## Consequences
- Parallel builds and much faster incremental compilation.
- Features are testable without Android dependencies at the domain layer.
- Cost: more Gradle boilerplate, and a shared `core:model` that needs discipline to stay thin.

## Alternatives considered
- Single module with packages — rejected, no enforcement.
- MVI-only without layers — rejected, pushes business rules into ViewModels.
