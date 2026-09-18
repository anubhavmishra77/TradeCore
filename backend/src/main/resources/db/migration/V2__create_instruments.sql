CREATE TABLE instruments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol       VARCHAR(32)  NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    exchange     VARCHAR(32)  NOT NULL,
    segment      VARCHAR(32)  NOT NULL,
    tick_size    NUMERIC(18,6) NOT NULL DEFAULT 0.05,
    lot_size     INTEGER      NOT NULL DEFAULT 1,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_instruments_symbol ON instruments (symbol);
CREATE INDEX idx_instruments_exchange ON instruments (exchange);
