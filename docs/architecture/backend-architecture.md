# Backend Architecture

## Stack
Ktor (Netty) + Exposed + HikariCP + PostgreSQL + Redis, Flyway for migrations.

## Package layout
Each bounded context (`auth`, `orders`, `execution`, `portfolio`, `wallet`, `marketdata`)
owns its routes, service, repository and models. Cross-cutting pieces live in
`common`, `plugins`, `config` and `observability`.

## Transactions
`TransactionManager` wraps service operations. Order placement, wallet blocking and
position updates must share one transaction — never split across service calls.

## Concurrency
Wallets and positions carry a `version` column for optimistic locking.
On conflict the operation retries up to three times, then fails with `CONFLICT`.

## Error handling
Services throw `AppException` carrying an `ErrorCode`; `StatusPages` maps those to
`ApiResponse.Error` with a stable machine-readable code.
