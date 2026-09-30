CREATE TABLE login_throttle (
    bucket_key VARCHAR(64) PRIMARY KEY,
    started_at BIGINT NOT NULL,
    attempts INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_login_throttle_expiry ON login_throttle(started_at);
