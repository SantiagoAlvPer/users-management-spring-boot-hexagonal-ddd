CREATE TABLE IF NOT EXISTS accounts (
    id              VARCHAR(36)    NOT NULL PRIMARY KEY,
    user_id         VARCHAR(36)    NOT NULL,
    account_number  VARCHAR(10)    NOT NULL,
    balance         DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency        VARCHAR(3)     NOT NULL DEFAULT 'COP',
    status          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_accounts_user_id UNIQUE (user_id),
    CONSTRAINT uk_accounts_number UNIQUE (account_number)
);
