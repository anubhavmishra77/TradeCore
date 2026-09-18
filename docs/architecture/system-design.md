# System Design

## Purpose
TradeCore is a simulated trading platform: an Android client and a Kotlin/Ktor backend
that together handle market data, order placement, execution, positions and wallet balances.

## Components
| Component | Responsibility |
|---|---|
| Android app | Compose UI, offline cache, WebSocket consumption |
| Ktor backend | REST + WebSocket API, order lifecycle, execution engine |
| PostgreSQL | System of record for users, orders, executions, positions, wallets |
| Redis | Quote cache, idempotency keys, WebSocket fan-out coordination |
| Prometheus / Grafana | Metrics and dashboards |

## Request path
1. Client sends `POST /orders` with an idempotency key.
2. `OrderValidator` checks instrument, quantity, price band and buying power.
3. Wallet funds are blocked inside the same transaction as the order insert.
4. `ExecutionEngine` fills the order against the current quote (market) or rests it (limit).
5. Executions update positions and unblock/settle wallet balances.
6. `UserWebSocketRoute` pushes order and position events to the client.

## Non-goals
- Real exchange connectivity
- Margin, derivatives, or short selling (phase 2)

## Open questions
- Partial fill policy for limit orders sitting across price updates
- Retention window for executions
