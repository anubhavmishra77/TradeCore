# Portfolio API

## GET /portfolio
```json
{
  "positions": [
    { "symbol": "...", "quantity": 10, "averagePrice": 100.0,
      "lastPrice": 104.0, "unrealizedPnl": 40.0, "realizedPnl": 0.0 }
  ],
  "totals": { "invested": 1000.0, "currentValue": 1040.0, "dayPnl": 40.0 }
}
```
Unrealized P&L is computed at read time against the cached quote.

## GET /portfolio/positions/{instrumentId}
Single position with its execution history.

## GET /wallet
`{ "currency": "INR", "availableBalance": 0.0, "blockedBalance": 0.0 }`

## POST /wallet/deposit
Demo-only top-up. Request: `{ "amount": 1000.0 }`
