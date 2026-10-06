ALTER TABLE admin_audit_logs
    ADD COLUMN action VARCHAR(80) NULL AFTER method,
    ADD COLUMN resource_type VARCHAR(80) NULL AFTER action,
    ADD COLUMN resource_id VARCHAR(120) NULL AFTER resource_type,
    ADD KEY idx_admin_audit_resource (resource_type, resource_id, created_at),
    ADD KEY idx_admin_audit_status_created (response_status, created_at);

UPDATE admin_audit_logs SET action = method WHERE action IS NULL;
