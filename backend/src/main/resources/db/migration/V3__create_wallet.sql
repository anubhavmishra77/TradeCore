CREATE TABLE wallets (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    currency         VARCHAR(8)    NOT NULL DEFAULT 'INR',
    available_balance NUMERIC(20,4) NOT NULL DEFAULT 0,
    blocked_balance   NUMERIC(20,4) NOT NULL DEFAULT 0,
    version          BIGINT        NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_wallet_user_currency UNIQUE (user_id, currency),
    CONSTRAINT chk_balances_non_negative CHECK (available_balance >= 0 AND blocked_balance >= 0)
);
