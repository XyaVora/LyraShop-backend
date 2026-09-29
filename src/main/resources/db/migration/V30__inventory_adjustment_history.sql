CREATE TABLE inventory_adjustments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BINARY(16) NOT NULL,
    variant_id BINARY(16) NOT NULL,
    admin_id BINARY(16) NOT NULL,
    stock_before INT NOT NULL,
    stock_after INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_inventory_adjustment_product_created (product_id, created_at),
    CONSTRAINT fk_inventory_adjustment_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_adjustment_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_adjustment_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_inventory_adjustment_stock CHECK (stock_before >= 0 AND stock_after >= 0),
    CONSTRAINT chk_inventory_adjustment_reason CHECK (CHAR_LENGTH(TRIM(reason)) > 0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
