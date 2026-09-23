CREATE TABLE customer_return_requests (
    id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_return_order (order_id),
    CONSTRAINT fk_customer_return_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_return_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE customer_return_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    return_request_id BINARY(16) NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_return_item (return_request_id, order_item_id),
    CONSTRAINT fk_customer_return_item_request FOREIGN KEY (return_request_id)
        REFERENCES customer_return_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_return_order_item FOREIGN KEY (order_item_id)
        REFERENCES order_items(id) ON DELETE RESTRICT,
    CONSTRAINT chk_customer_return_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE customer_return_evidence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    return_request_id BINARY(16) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_customer_return_evidence_request FOREIGN KEY (return_request_id)
        REFERENCES customer_return_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
