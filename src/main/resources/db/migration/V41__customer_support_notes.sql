CREATE TABLE customer_support_notes (
    id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,
    created_by BINARY(16) NOT NULL,
    note VARCHAR(2000) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_customer_support_notes_customer_created (customer_id, created_at),
    CONSTRAINT fk_customer_support_notes_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_customer_support_notes_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);
