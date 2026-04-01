-- ============================================================
-- V5: Performance Indexes
-- ============================================================

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_source ON transactions(source_account_id);
CREATE INDEX idx_transactions_dest ON transactions(destination_account_id);
CREATE INDEX idx_transactions_created ON transactions(created_at);
CREATE INDEX idx_transactions_ref ON transactions(reference_number);
CREATE INDEX idx_ledger_transaction ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_account ON ledger_entries(account_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_mfa_txref ON mfa_tokens(transaction_reference);
CREATE INDEX idx_stock_user ON stock_positions(user_id);

-- Composite indexes required by V5 spec
CREATE INDEX idx_ledger_composite ON ledger_entries(account_id, created_at);
CREATE INDEX idx_txn_composite ON transactions(source_account_id, status, created_at);
