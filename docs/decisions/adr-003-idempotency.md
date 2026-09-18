# ADR-003: Idempotent order placement

- Status: Accepted
- Date: 2026-01-15

## Context
A mobile client on a flaky network will retry `POST /orders`. Without protection, a retry
after a successful-but-unacknowledged write places a second real order.

## Decision
The client generates a UUID per order intent and sends it as `Idempotency-Key`. The backend
stores it on the order row with a unique constraint on `(user_id, idempotency_key)`.
`IdempotencyService` catches the constraint violation and returns the original order with `200`.
Keys are also cached in Redis for 60 minutes to short-circuit the common case.

## Consequences
- Retries are safe and cheap; the database is the final arbiter, not the cache.
- The client must persist the key across process death, before sending the request.
- Keys are scoped per user, so collisions across users are harmless.

## Alternatives considered
- Server-side dedup on a hash of the payload — rejected, two identical intentional orders
  are legitimate.
