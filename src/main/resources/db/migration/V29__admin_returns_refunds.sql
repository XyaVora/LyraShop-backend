ALTER TABLE orders
    ADD COLUMN refunded_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 AFTER return_requested_at;

CREATE TABLE order_refunds (
    id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    return_request_id BINARY(16) NULL,
    processed_by BINARY(16) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    reference VARCHAR(100) NOT NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_refund_reference (reference),
    KEY idx_order_refund_order_created (order_id, created_at),
    CONSTRAINT fk_order_refund_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
    CONSTRAINT fk_order_refund_return FOREIGN KEY (return_request_id) REFERENCES customer_return_requests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_order_refund_admin FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_refund_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
