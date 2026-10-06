CREATE TABLE admin_notification_reads (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_id BINARY(16) NOT NULL,
    notification_key VARCHAR(120) NOT NULL,
    read_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_notification_read (admin_id, notification_key),
    KEY idx_admin_notification_read_admin (admin_id, read_at),
    CONSTRAINT fk_admin_notification_read_admin
        FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
