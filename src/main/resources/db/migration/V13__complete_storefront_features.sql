ALTER TABLE orders
    ADD COLUMN subtotal_amount DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER user_id,
    ADD COLUMN discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER subtotal_amount,
    ADD COLUMN shipping_fee DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER discount_amount;

UPDATE orders
SET subtotal_amount = total_amount
WHERE subtotal_amount = 0;

CREATE TABLE search_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BINARY(16) NOT NULL,
    query VARCHAR(100) NOT NULL,
    searched_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_search_history PRIMARY KEY (id),
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_search_history_user_query UNIQUE (user_id, query),
    CONSTRAINT chk_search_history_query CHECK (CHAR_LENGTH(TRIM(query)) > 0),
    INDEX idx_search_history_user_time (user_id, searched_at DESC)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE wishlist_shares (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_wishlist_shares PRIMARY KEY (id),
    CONSTRAINT fk_wishlist_shares_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_wishlist_shares_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
