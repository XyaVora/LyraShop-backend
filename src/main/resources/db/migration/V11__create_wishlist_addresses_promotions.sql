CREATE TABLE wishlist_items (
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

CREATE TABLE shipping_addresses (
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

CREATE TABLE promotions (
    id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255) NULL,
    description VARCHAR(1000) NULL,
    badge VARCHAR(100) NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_promotions PRIMARY KEY (id),
    CONSTRAINT chk_promotions_title CHECK (CHAR_LENGTH(TRIM(title)) > 0),
    CONSTRAINT chk_promotions_dates CHECK (ends_at > starts_at),
    CONSTRAINT chk_promotions_active CHECK (is_active IN (0, 1)),
    INDEX idx_promotions_active_window (is_active, starts_at, ends_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE promotion_products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    promotion_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    sale_price DECIMAL(12,2) NOT NULL,
    original_price DECIMAL(12,2) NOT NULL,
    discount_percent INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_promotion_products PRIMARY KEY (id),
    CONSTRAINT uk_promotion_products_promotion_product UNIQUE (promotion_id, product_id),
    CONSTRAINT fk_promotion_products_promotion FOREIGN KEY (promotion_id) REFERENCES promotions (id) ON DELETE CASCADE,
    CONSTRAINT fk_promotion_products_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT chk_promotion_products_prices CHECK (sale_price >= 0 AND original_price >= sale_price),
    CONSTRAINT chk_promotion_products_discount CHECK (discount_percent BETWEEN 0 AND 100),
    CONSTRAINT chk_promotion_products_sort CHECK (sort_order >= 0),
    INDEX idx_promotion_products_order (promotion_id, sort_order, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

INSERT IGNORE INTO promotions
    (id, title, subtitle, description, badge, starts_at, ends_at, is_active, created_at, updated_at)
VALUES
    (UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), 'Mùa Sale', 'Đặc quyền LYRA',
     'Ưu đãi có thời hạn dành cho các thiết kế được chọn.', 'Ưu đãi có hạn',
     NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, NOW(), NOW());

INSERT IGNORE INTO promotion_products
    (promotion_id, product_id, sale_price, original_price, discount_percent, sort_order)
SELECT UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), id,
       ROUND(base_price * 0.8, 2), base_price, 20,
       ROW_NUMBER() OVER (ORDER BY created_at, id)
FROM products
WHERE is_active = 1
ORDER BY created_at, id
LIMIT 8;
