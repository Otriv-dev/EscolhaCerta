CREATE TABLE security_audit (
    id VARCHAR(36) PRIMARY KEY,
    occurred_at TIMESTAMP(6) NOT NULL,
    actor VARCHAR(80) NOT NULL,
    action VARCHAR(60) NOT NULL,
    object_id VARCHAR(80) NOT NULL,
    result VARCHAR(16) NOT NULL,
    request_id VARCHAR(36) NOT NULL
);
CREATE INDEX idx_security_audit_time ON security_audit(occurred_at);
CREATE INDEX idx_security_audit_actor_time ON security_audit(actor, occurred_at);
