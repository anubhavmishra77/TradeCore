CREATE TABLE orders (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users (id),
    instrument_id    UUID NOT NULL REFERENCES instruments (id),
    idempotency_key  VARCHAR(80)  NOT NULL,
    side             VARCHAR(8)   NOT NULL,
    type             VARCHAR(16)  NOT NULL,
    status           VARCHAR(24)  NOT NULL,
    quantity         NUMERIC(20,4) NOT NULL,
    filled_quantity  NUMERIC(20,4) NOT NULL DEFAULT 0,
    limit_price      NUMERIC(20,4),
    average_price    NUMERIC(20,4),
    reject_reason    TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_orders_idempotency UNIQUE (user_id, idempotency_key),
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_orders_user_status ON orders (user_id, status);
CREATE INDEX idx_orders_instrument ON orders (instrument_id);
