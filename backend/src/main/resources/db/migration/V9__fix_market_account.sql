-- ============================================================
-- V9: Fix account_number column width and market account
-- Widens account_number to 25 chars to support system accounts
-- ============================================================

-- Widen account_number column to support longer system account numbers
ALTER TABLE accounts ALTER COLUMN account_number TYPE VARCHAR(25);

-- Rename the system market account to a shorter valid name (VC-MARKET-001 = 13 chars)
-- This handles cases where V6 succeeded with a DBMS that auto-truncates
-- or where it was manually inserted
UPDATE accounts 
SET account_number = 'VC-MARKET-001'
WHERE account_number IN ('VC-SYSTEM-MARKET-001', 'VC-SYSTEM-MARKET001');

-- If market account doesn't exist yet (V6 failed), insert it now
INSERT INTO accounts (id, account_number, user_id, account_type, balance, currency, active)
SELECT 
    '77eebc99-9c0b-4ef8-bb6d-6bb9bd380a77',
    'VC-MARKET-001',
    'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66',
    'SYSTEM',
    1000000000.0000,
    'USD',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM accounts WHERE account_number = 'VC-MARKET-001'
);

-- Ensure system user exists (idempotent)
INSERT INTO users (id, username, email, full_name, keycloak_id, role, active)
SELECT 
    'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66',
    'system.market',
    'market@vaultcore.com',
    'System Market Clearing',
    NULL,
    'SYSTEM',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66'
);
