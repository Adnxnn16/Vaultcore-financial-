-- ============================================================
-- V2: MFA Tokens
-- ============================================================

CREATE TABLE mfa_tokens (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id),
    token_hash              VARCHAR(255) NOT NULL,
    transaction_reference   VARCHAR(50) NOT NULL,
    used                    BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at              TIMESTAMP NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);
