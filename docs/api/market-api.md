# Market API

## GET /instruments
Query: `exchange`, `segment`, `limit`, `cursor`
Response: paginated list of instruments.

## GET /instruments/search?q=
Prefix search on symbol and name. Returns at most 25 results.

## GET /instruments/{id}/quote
Latest quote: `{ "symbol": "...", "ltp": 0.0, "bid": 0.0, "ask": 0.0, "ts": 0 }`
Served from the Redis cache; falls back to the provider on a miss.

## WS /ws/market
Subscribe: `{ "type": "SUBSCRIBE", "symbols": ["..."] }`
Unsubscribe: `{ "type": "UNSUBSCRIBE", "symbols": ["..."] }`
