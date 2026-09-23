CREATE TABLE return_evidence_uploads (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    url VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    consumed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_return_evidence_upload_url UNIQUE (url),
    CONSTRAINT fk_return_evidence_upload_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_return_evidence_upload_expiry CHECK (expires_at > created_at),
    INDEX idx_return_evidence_upload_cleanup (consumed_at, expires_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
