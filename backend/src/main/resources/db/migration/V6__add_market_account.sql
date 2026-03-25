-- ============================================================
-- V6: System Market & Clearing Accounts
-- STRICT: Every stock trade must balance with a market account
-- ============================================================

-- System Core User (Non-interactive)
INSERT INTO users (id, username, email, full_name, keycloak_id, role, active)
VALUES ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'system.market', 'market@vaultcore.com', 'System Market Clearing', NULL, 'SYSTEM', TRUE);

-- Global Stock Market Clearing Account
INSERT INTO accounts (id, account_number, user_id, account_type, balance, currency, active)
VALUES ('77eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'VC-MARKET-001', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'SYSTEM', 1000000000.0000, 'USD', TRUE);
