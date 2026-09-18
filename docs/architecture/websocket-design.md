# WebSocket Design

## Channels
- `/ws/market` — public quote stream, subscribe by symbol
- `/ws/user` — authenticated stream of order, execution, position and wallet events

## Message envelope
```json
{ "type": "QUOTE", "seq": 1042, "ts": 1737100000, "payload": { } }
```

## Sequencing
`EventSequenceManager` assigns a monotonic `seq` per connection. The client tracks
the last seen `seq`; on reconnect it sends it back and the server replays or tells
the client to resync via REST.

## Subscriptions
`SubscriptionManager` keeps a symbol → connections map. Quote updates fan out only
to subscribed connections.

## Reconnect
Client uses exponential backoff with jitter (1s → 30s cap) in `WebSocketReconnectPolicy`.
See ADR-002.
