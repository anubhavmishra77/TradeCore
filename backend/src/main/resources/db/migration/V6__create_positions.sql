CREATE TABLE positions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    instrument_id     UUID NOT NULL REFERENCES instruments (id),
    quantity          NUMERIC(20,4) NOT NULL DEFAULT 0,
    average_price     NUMERIC(20,4) NOT NULL DEFAULT 0,
    realized_pnl      NUMERIC(20,4) NOT NULL DEFAULT 0,
    version           BIGINT        NOT NULL DEFAULT 0,
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_position_user_instrument UNIQUE (user_id, instrument_id)
);

CREATE INDEX idx_positions_user ON positions (user_id);
