-- ============================================================
-- VaultCore Financial — Initial Schema
-- STRICT: Immutable ledger, double-entry bookkeeping
-- ============================================================

-- Users table
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    full_name       VARCHAR(200) NOT NULL,
    keycloak_id     VARCHAR(255) UNIQUE,
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    mfa_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- Accounts table with balance >= 0 constraint
CREATE TABLE accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number  VARCHAR(20) NOT NULL UNIQUE,
    user_id         UUID NOT NULL REFERENCES users(id),
    account_type    VARCHAR(20) NOT NULL DEFAULT 'CHECKING',
    balance         NUMERIC(19,4) NOT NULL DEFAULT 0.0000
                    CONSTRAINT positive_balance CHECK (balance >= 0),
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    version         BIGINT NOT NULL DEFAULT 0
);

-- Transactions table
CREATE TABLE transactions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_number        VARCHAR(50) NOT NULL UNIQUE,
    source_account_id       UUID NOT NULL REFERENCES accounts(id),
    destination_account_id  UUID NOT NULL REFERENCES accounts(id),
    amount                  NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency                VARCHAR(3) NOT NULL DEFAULT 'USD',
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description             VARCHAR(500),
    transaction_type        VARCHAR(20) NOT NULL DEFAULT 'TRANSFER',
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMP,
    CONSTRAINT different_accounts CHECK (source_account_id != destination_account_id)
);

-- Immutable ledger entries — NO UPDATE, NO DELETE
CREATE TABLE ledger_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id  UUID NOT NULL REFERENCES transactions(id),
    account_id      UUID NOT NULL REFERENCES accounts(id),
    entry_type      VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount          NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    balance_after   NUMERIC(19,4) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Prevent UPDATE on ledger_entries
CREATE OR REPLACE FUNCTION prevent_ledger_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Ledger entries are immutable. UPDATE operations are not allowed.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_ledger_update
    BEFORE UPDATE ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION prevent_ledger_update();

-- Prevent DELETE on ledger_entries
CREATE OR REPLACE FUNCTION prevent_ledger_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Ledger entries are immutable. DELETE operations are not allowed.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_ledger_delete
    BEFORE DELETE ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION prevent_ledger_delete();

-- Seed data: demo user + accounts
INSERT INTO users (id, username, email, full_name, keycloak_id, role)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'john.doe', 'john@vaultcore.com', 'John Doe', NULL, 'USER'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'admin', 'admin@vaultcore.com', 'System Admin', NULL, 'ADMIN');

INSERT INTO accounts (id, account_number, user_id, account_type, balance, currency)
VALUES
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'VC-100001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'CHECKING', 50000.0000, 'USD'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'VC-100002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SAVINGS', 125000.0000, 'USD'),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'VC-200001', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'CHECKING', 75000.0000, 'USD');
