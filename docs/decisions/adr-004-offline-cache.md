# ADR-004: Room as the offline source of truth

- Status: Accepted
- Date: 2026-01-18

## Context
Users open the app in transit. A blank screen while a request is in flight is the worst case,
and instrument lists and order history change slowly.

## Decision
Repositories expose `Flow` backed by Room. Network responses are written to Room and the UI
observes the database, never the network call directly. Quotes are the exception: they are
live-only and held in memory, since a stale price is worse than no price.

## Consequences
- Instant cold-start render, and the app stays usable offline for browsing.
- Every remote model needs an entity and a mapper.
- Cache invalidation rules must be explicit per entity; staleness is shown in the UI.

## Alternatives considered
- In-memory cache only — rejected, lost on process death.
- Caching quotes in Room — rejected, write amplification and stale-price risk.
