CREATE TABLE email_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    from_address VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME(6) NOT NULL,
    sent_at DATETIME(6) NULL,
    last_error VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT chk_email_outbox_status CHECK (status IN ('PENDING','SENT','FAILED')),
    CONSTRAINT chk_email_outbox_attempts CHECK (attempts >= 0),
    INDEX idx_email_outbox_due (status, next_attempt_at, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
