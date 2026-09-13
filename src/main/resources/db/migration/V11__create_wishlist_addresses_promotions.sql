CREATE TABLE IF NOT EXISTS wishlist_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_wishlist_items PRIMARY KEY (id),
    CONSTRAINT uk_wishlist_items_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_wishlist_items_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    INDEX idx_wishlist_items_user_created (user_id, created_at, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS shipping_addresses (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address_line VARCHAR(500) NOT NULL,
    ward VARCHAR(255) NULL,
    district VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_shipping_addresses PRIMARY KEY (id),
    CONSTRAINT fk_shipping_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_shipping_addresses_recipient CHECK (CHAR_LENGTH(TRIM(recipient_name)) > 0),
    CONSTRAINT chk_shipping_addresses_phone CHECK (CHAR_LENGTH(TRIM(phone)) > 0),
    CONSTRAINT chk_shipping_addresses_line CHECK (CHAR_LENGTH(TRIM(address_line)) > 0),
    CONSTRAINT chk_shipping_addresses_district CHECK (CHAR_LENGTH(TRIM(district)) > 0),
    CONSTRAINT chk_shipping_addresses_city CHECK (CHAR_LENGTH(TRIM(city)) > 0),
    CONSTRAINT chk_shipping_addresses_default CHECK (is_default IN (0, 1)),
    INDEX idx_shipping_addresses_user_default (user_id, is_default, created_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS promotions (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    discount_percent INT NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_promotions PRIMARY KEY (id),
    CONSTRAINT chk_promotions_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_promotions_discount CHECK (discount_percent BETWEEN 0 AND 100),
    CONSTRAINT chk_promotions_dates CHECK (end_at > start_at),
    CONSTRAINT chk_promotions_active CHECK (is_active IN (0, 1)),
    INDEX idx_promotions_active_window (is_active, start_at, end_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS promotion_products (
    promotion_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    sale_price DECIMAL(12,2) NOT NULL,
    original_price DECIMAL(12,2) NOT NULL,
    discount_percent INT NOT NULL,
    CONSTRAINT pk_promotion_products PRIMARY KEY (promotion_id, product_id),
    CONSTRAINT fk_promotion_products_promotion FOREIGN KEY (promotion_id) REFERENCES promotions (id) ON DELETE CASCADE,
    CONSTRAINT fk_promotion_products_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT chk_promotion_products_prices CHECK (sale_price >= 0 AND original_price >= sale_price),
    CONSTRAINT chk_promotion_products_discount CHECK (discount_percent BETWEEN 0 AND 100),
    INDEX idx_promotion_products_product (product_id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

INSERT IGNORE INTO promotions
    (id, name, description, discount_percent, start_at, end_at, is_active, created_at, updated_at)
VALUES
    (UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), 'Mùa Sale',
     'Ưu đãi có thời hạn dành cho các thiết kế được chọn.', 20,
     NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, NOW(), NOW());

INSERT IGNORE INTO promotion_products
    (promotion_id, product_id, sale_price, original_price, discount_percent)
SELECT UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), id,
       ROUND(base_price * 0.8, 2), base_price, 20
FROM products
WHERE is_active = 1
ORDER BY created_at, id
LIMIT 8;
