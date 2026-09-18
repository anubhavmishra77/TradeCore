# Android paper-trading demo

The Android app runs independently of the Ktor backend. Launch `app` from Android
Studio, enter a display name, and start with $10,000 virtual USD. This is a local
demo profile, not server authentication.

## Build

Use JDK 17 or 21 with the pinned Gradle 8.7 wrapper (AGP 8.5 / Kotlin 2.0).
In Android Studio, select that JDK under Settings → Build Tools → Gradle → Gradle JDK.
The latest Android Studio bundled JDK 25 is not compatible with Gradle 8.7.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# With an emulator running:
./gradlew :app:connectedDebugAndroidTest
```

## Working flows

- Market search and gainers filter across BTC, ETH, SOL, LINK, AVAX, DOGE.
- Persistent watchlist, asset details, 24-hour hourly-close charts with retry.
- Buy/sell market orders with quantity validation, reviewed fill price, explicit
  confirmation, available cash/holdings checks, and a completion receipt.
- Portfolio value, cost basis, realized and unrealized P&L.
- Buy/sell-filtered order history with quote source and fill details.
- Wallet balance, virtual top-ups, recent ledger entries, and confirmed reset.
- Atomic local account snapshots preserve cash, holdings, orders, funding, and
  watchlist across app restarts. Failed saves do not publish a successful trade.

## Market data

Public Coinbase Exchange endpoints, no API key or account required:

- `https://api.exchange.coinbase.com/products/{SYMBOL}-USD/stats`
- `https://api.exchange.coinbase.com/products/{SYMBOL}-USD/candles?granularity=3600`

Reference: https://docs.cdp.coinbase.com/api-reference/exchange-api/rest-api/products/get-product-stats

Quotes refresh at startup or on demand. Each asset shows sample/fetched/stale status.
The initial prices are fixed, clearly labeled samples, **not current prices**.
Network failures keep the previous quote; fetched quotes older than two minutes
cannot be used for orders. Sample quotes remain tradable for offline practice.
Charts contain only API candles; failures show an unavailable state instead of a
fabricated graph. A network connection and provider availability are needed for
fetched quotes and charts. USD is used throughout.

## Implementation and scope

The active standalone flow is in `app/.../demo`: pure decimal ledger in
`PaperTrading.kt`, local persistence/public API adapter in `DemoRepository.kt`,
state/actions in `TradingViewModel.kt`, navigation in `TradeCoreApp.kt`, and separate Compose screen files.
`MainActivity` launches this flow. The existing backend-connected feature-module
stubs remain scaffolding for future integration; they are not part of the demo flow.

This demo has no real orders, broker connection, cloud account sync, limit orders,
fees, or slippage model. Clearing app storage removes the local account.

## Manual smoke test

1. Create a demo profile; verify the $10,000 starting balance.
2. Search BTC, open its detail, buy 0.01, review, then confirm.
3. Verify the holding in Portfolio, filled order in Orders, and debit in Wallet.
4. Sell part of the position; try overselling and an unaffordable buy.
5. Add $1,000 demo funds; verify this does not count as profit.
6. Save/remove a watchlist asset, restart the app, verify persisted state.
7. Disconnect networking and refresh; check source/error labels and chart retry.
8. Reset the account, confirm, verify $10,000 and empty holdings/order history.
