ALTER TABLE orders
    ADD COLUMN idempotency_key VARCHAR(64) NULL AFTER user_id,
    ADD COLUMN loyalty_coins_used BIGINT NOT NULL DEFAULT 0 AFTER discount_amount,
    ADD COLUMN loyalty_discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER loyalty_coins_used,
    ADD CONSTRAINT chk_orders_loyalty_coins_used CHECK (loyalty_coins_used >= 0),
    ADD CONSTRAINT chk_orders_loyalty_discount CHECK (loyalty_discount_amount >= 0),
    ADD CONSTRAINT uk_orders_user_idempotency UNIQUE (user_id, idempotency_key);

ALTER TABLE loyalty_transactions
    ADD COLUMN order_id BINARY(16) NULL AFTER user_id,
    ADD CONSTRAINT fk_loyalty_transactions_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT uk_loyalty_transactions_type_order UNIQUE (transaction_type, order_id);
