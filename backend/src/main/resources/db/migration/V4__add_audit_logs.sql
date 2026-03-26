-- ============================================================
-- V4: Audit Logs and Append-Only Triggers
-- ============================================================

CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       VARCHAR(50),
    method_name     VARCHAR(200) NOT NULL,
    parameters      TEXT,
    result          TEXT,
    ip_address      VARCHAR(50),
    status          VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Prevent UPDATE on audit_logs
CREATE OR REPLACE FUNCTION prevent_audit_log_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit logs are append-only. UPDATE operations are not allowed.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_audit_log_update
    BEFORE UPDATE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_update();

-- Prevent DELETE on audit_logs
CREATE OR REPLACE FUNCTION prevent_audit_log_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit logs are append-only. DELETE operations are not allowed.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_audit_log_delete
    BEFORE DELETE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_delete();
