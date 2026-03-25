-- ============================================================
-- V8: Reconcile Schema with JPA Entities
-- Fixes validation errors preventing backend startup
-- ============================================================

-- 1. Accounts Table
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- 2. MFA Tokens Table
-- Aligning with MfaToken.java entity
ALTER TABLE mfa_tokens RENAME COLUMN token_hash TO token;
ALTER TABLE mfa_tokens RENAME COLUMN expires_at TO expiry;
ALTER TABLE mfa_tokens ADD COLUMN otp VARCHAR(10) NOT NULL DEFAULT '000000';
ALTER TABLE mfa_tokens DROP COLUMN IF EXISTS transaction_reference;
-- Re-enable NOT NULL without default for future entries if needed, 
-- but for migration, a default is safer.

-- 3. Transactions Table
-- Map the mfa_token field from Transaction.java
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS mfa_token VARCHAR(255);

-- 4. Stock Positions Table
-- Map updated_at from StockPosition.java
ALTER TABLE stock_positions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
