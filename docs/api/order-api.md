# Order API

## POST /orders
Header: `Idempotency-Key: <uuid>` (required)
Request:
```json
{ "instrumentId": "...", "side": "BUY", "type": "LIMIT", "quantity": 10, "limitPrice": 101.5 }
```
Response `201`: the created order with `status` of `PENDING`, `FILLED` or `REJECTED`.
Replaying the same key returns the original order with `200`.
Errors: `INSUFFICIENT_FUNDS`, `INSTRUMENT_INACTIVE`, `INVALID_QUANTITY`, `PRICE_OUT_OF_BAND`

## DELETE /orders/{id}
Cancels an open order. Errors: `ORDER_NOT_CANCELLABLE`, `ORDER_NOT_FOUND`

## GET /orders
Query: `status`, `from`, `to`, `cursor`. Newest first.

## GET /orders/{id}/executions
Fill history for one order.
