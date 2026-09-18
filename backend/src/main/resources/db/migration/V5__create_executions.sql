CREATE TABLE executions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id      UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    sequence_no   BIGSERIAL    NOT NULL,
    quantity      NUMERIC(20,4) NOT NULL,
    price         NUMERIC(20,4) NOT NULL,
    fee           NUMERIC(20,4) NOT NULL DEFAULT 0,
    executed_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_exec_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_executions_order ON executions (order_id);
