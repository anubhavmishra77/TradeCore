# ADR-002: WebSockets for live data

- Status: Accepted
- Date: 2026-01-12

## Context
Quotes and order status need to reach the client within a second. Polling at that rate
across many instruments is wasteful and still feels laggy.

## Decision
Two WebSocket channels: a public market channel with per-symbol subscriptions, and an
authenticated user channel for order, execution, position and wallet events. Every message
carries a monotonic sequence number so the client can detect gaps.

## Consequences
- Server keeps connection state; `ConnectionManager` and `SubscriptionManager` are now
  load-bearing and need their own tests.
- Reconnect handling and resync become a first-class client concern.
- Horizontal scaling later requires Redis pub/sub between instances.

## Alternatives considered
- SSE — simpler, but one-directional; subscription changes would need a second channel.
- Long polling — rejected on latency and battery.
