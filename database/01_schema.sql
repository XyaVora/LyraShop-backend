-- Generated from Flyway V1-V34. Do not edit by hand.
-- MySQL 8.0+. DESTRUCTIVE: recreates the local inspection database.
SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS lyrashop_db;

CREATE DATABASE lyrashop_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE lyrashop_db;

-- =============================================================================
-- V1__create_identity_tables - structure
-- =============================================================================

CREATE TABLE users (
    id BINARY(16) NOT NULL,
    email VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL,
    password VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    role VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_email_not_blank CHECK (CHAR_LENGTH(TRIM(email)) > 0),
    CONSTRAINT chk_users_password_not_blank CHECK (CHAR_LENGTH(password) > 0),
    CONSTRAINT chk_users_full_name_not_blank CHECK (CHAR_LENGTH(TRIM(full_name)) > 0),
    CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER', 'ADMIN')),
    CONSTRAINT chk_users_active CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_users_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE refresh_sessions (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    family_id BINARY(16) NOT NULL,
    token_hash BINARY(32) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    consumed_at DATETIME(6) NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_refresh_sessions PRIMARY KEY (id),
    CONSTRAINT uk_refresh_sessions_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_refresh_sessions_expiry CHECK (expires_at > created_at),
    CONSTRAINT chk_refresh_sessions_consumed
        CHECK (consumed_at IS NULL OR consumed_at >= created_at),
    CONSTRAINT chk_refresh_sessions_revoked
        CHECK (revoked_at IS NULL OR revoked_at >= created_at),
    CONSTRAINT chk_refresh_sessions_version CHECK (version >= 0),
    INDEX idx_refresh_sessions_user_state (user_id, revoked_at),
    INDEX idx_refresh_sessions_family_state (family_id, revoked_at),
    INDEX idx_refresh_sessions_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V2__create_categories - structure
-- =============================================================================

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    description TEXT NULL,
    parent_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT chk_categories_name_not_blank
        CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_categories_slug_format
        CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT chk_categories_active CHECK (is_active IN (0, 1)),
    INDEX idx_categories_active_name (is_active, name, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V3__create_products - structure
-- =============================================================================

CREATE TABLE products (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    description TEXT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    category_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT chk_products_name_not_blank
        CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_products_slug_format
        CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT chk_products_price
        CHECK (base_price >= 0),
    CONSTRAINT chk_products_active CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_products_version CHECK (version >= 0),
    INDEX idx_products_active_category (is_active, category_id, name, id),
    INDEX idx_products_active_price (is_active, base_price, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V4__create_product_variants - structure
-- =============================================================================

CREATE TABLE product_variants (
    id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    sku VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    size VARCHAR(20) NOT NULL,
    color VARCHAR(50) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_product_variants PRIMARY KEY (id),
    CONSTRAINT uk_product_variants_sku UNIQUE (sku),
    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT chk_product_variants_sku_not_blank CHECK (CHAR_LENGTH(TRIM(sku)) > 0),
    CONSTRAINT chk_product_variants_size_not_blank CHECK (CHAR_LENGTH(TRIM(size)) > 0),
    CONSTRAINT chk_product_variants_color_not_blank CHECK (CHAR_LENGTH(TRIM(color)) > 0),
    CONSTRAINT chk_product_variants_price CHECK (price >= 0),
    CONSTRAINT chk_product_variants_stock CHECK (stock >= 0),
    CONSTRAINT chk_product_variants_active CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_product_variants_version CHECK (version >= 0),
    INDEX idx_product_variants_product_active (product_id, is_active, id),
    INDEX idx_product_variants_active_stock (is_active, stock, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V5__create_product_images - structure
-- =============================================================================

CREATE TABLE product_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BINARY(16) NOT NULL,
    variant_id BINARY(16) NULL,
    url VARCHAR(2048) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_product_images PRIMARY KEY (id),
    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_images_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE SET NULL,
    CONSTRAINT chk_product_images_url_not_blank CHECK (CHAR_LENGTH(TRIM(url)) > 0),
    CONSTRAINT chk_product_images_primary CHECK (is_primary IN (0, 1)),
    CONSTRAINT chk_product_images_sort_order CHECK (sort_order >= 0),
    INDEX idx_product_images_product_sort (product_id, sort_order, id),
    INDEX idx_product_images_variant (variant_id, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V6__create_carts - structure
-- =============================================================================

CREATE TABLE carts (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_carts PRIMARY KEY (id),
    CONSTRAINT uk_carts_user UNIQUE (user_id),
    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cart_id BINARY(16) NOT NULL,
    variant_id BINARY(16) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_cart_items PRIMARY KEY (id),
    CONSTRAINT uk_cart_items_cart_variant UNIQUE (cart_id, variant_id),
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE RESTRICT,
    CONSTRAINT chk_cart_items_quantity CHECK (quantity >= 1)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V7__create_orders - structure
-- =============================================================================

CREATE TABLE orders (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    payment_status VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'UNPAID',
    shipping_address TEXT NOT NULL,
    shipping_phone VARCHAR(20) NOT NULL,
    note TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_orders_total CHECK (total_amount >= 0),
    CONSTRAINT chk_orders_status CHECK (status IN ('PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED','CANCELLED')),
    CONSTRAINT chk_orders_payment_method CHECK (payment_method IN ('COD')),
    CONSTRAINT chk_orders_payment_status CHECK (payment_status IN ('UNPAID','PAID','FAILED','REFUNDED')),
    CONSTRAINT chk_orders_shipping_address CHECK (CHAR_LENGTH(TRIM(shipping_address)) > 0),
    CONSTRAINT chk_orders_shipping_phone CHECK (CHAR_LENGTH(TRIM(shipping_phone)) > 0),
    INDEX idx_orders_user_created (user_id, created_at, id),
    INDEX idx_orders_status_created (status, created_at, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BINARY(16) NOT NULL,
    variant_id BINARY(16) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    size VARCHAR(20) NOT NULL,
    color VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_items_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_subtotal CHECK (subtotal >= 0),
    INDEX idx_order_items_order (order_id, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V8__create_reviews - structure
-- =============================================================================

CREATE TABLE reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    rating INT NOT NULL,
    comment TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT uk_reviews_product_user UNIQUE (product_id, user_id),
    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    INDEX idx_reviews_product_created (product_id, created_at, id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V9__allow_vnpay_payment_method - structure
-- =============================================================================

ALTER TABLE orders DROP CHECK chk_orders_payment_method;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_payment_method
        CHECK (payment_method IN ('COD', 'VNPAY'));


-- =============================================================================
-- V11__create_wishlist_addresses_promotions - structure
-- =============================================================================

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


-- =============================================================================
-- V13__complete_storefront_features - structure
-- =============================================================================

ALTER TABLE orders
    ADD COLUMN subtotal_amount DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER user_id,
    ADD COLUMN discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER subtotal_amount,
    ADD COLUMN shipping_fee DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER discount_amount;

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


-- =============================================================================
-- V14__snapshot_product_id_on_order_items - structure
-- =============================================================================

ALTER TABLE order_items
    ADD COLUMN product_id BINARY(16) NULL AFTER variant_id;

ALTER TABLE order_items
    MODIFY COLUMN product_id BINARY(16) NOT NULL,
    ADD CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    ADD INDEX idx_order_items_product (product_id, order_id);


-- =============================================================================
-- V15__complete_customer_order_flow - structure
-- =============================================================================

ALTER TABLE orders
    ADD COLUMN gift_wrap_fee DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER shipping_fee,
    ADD COLUMN voucher_code VARCHAR(30) NULL AFTER note,
    ADD COLUMN gift_wrap BOOLEAN NOT NULL DEFAULT FALSE AFTER voucher_code,
    ADD COLUMN gift_message VARCHAR(500) NULL AFTER gift_wrap,
    ADD COLUMN cancellation_reason VARCHAR(500) NULL AFTER gift_message,
    ADD COLUMN shipping_carrier VARCHAR(100) NULL AFTER cancellation_reason,
    ADD COLUMN tracking_code VARCHAR(100) NULL AFTER shipping_carrier,
    ADD COLUMN tracking_url VARCHAR(500) NULL AFTER tracking_code,
    ADD COLUMN estimated_delivery_at DATETIME(6) NULL AFTER tracking_url,
    ADD COLUMN delivered_at DATETIME(6) NULL AFTER estimated_delivery_at,
    ADD COLUMN return_status VARCHAR(30) NULL AFTER delivered_at,
    ADD COLUMN return_reason VARCHAR(1000) NULL AFTER return_status,
    ADD COLUMN return_requested_at DATETIME(6) NULL AFTER return_reason,
    ADD CONSTRAINT chk_orders_gift_wrap_fee CHECK (gift_wrap_fee >= 0),
    ADD INDEX idx_orders_tracking_code (tracking_code);


-- =============================================================================
-- V16__create_vouchers - structure
-- =============================================================================

CREATE TABLE vouchers (
    id BINARY(16) NOT NULL,
    code VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    label VARCHAR(255) NOT NULL,
    voucher_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    discount_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL DEFAULT 0,
    max_discount_amount DECIMAL(12,2) NULL,
    minimum_order_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    total_usage_limit INT NULL,
    per_user_limit INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_vouchers PRIMARY KEY (id),
    CONSTRAINT uk_vouchers_code UNIQUE (code),
    CONSTRAINT chk_vouchers_type CHECK (voucher_type IN ('discount','shipping')),
    CONSTRAINT chk_vouchers_discount_type CHECK (discount_type IN ('PERCENT','FIXED','FREESHIP')),
    CONSTRAINT chk_vouchers_amounts CHECK (discount_value >= 0 AND minimum_order_amount >= 0),
    CONSTRAINT chk_vouchers_dates CHECK (ends_at > starts_at),
    CONSTRAINT chk_vouchers_limits CHECK ((total_usage_limit IS NULL OR total_usage_limit > 0) AND per_user_limit > 0),
    INDEX idx_vouchers_active_dates (is_active, starts_at, ends_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE voucher_redemptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    voucher_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    redeemed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_voucher_redemptions PRIMARY KEY (id),
    CONSTRAINT fk_voucher_redemptions_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_voucher_redemptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_voucher_redemptions_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT uk_voucher_redemptions_order UNIQUE (order_id),
    INDEX idx_voucher_redemptions_voucher (voucher_id),
    INDEX idx_voucher_redemptions_user_voucher (user_id, voucher_id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V17__create_password_reset_tokens - structure
-- =============================================================================

CREATE TABLE password_reset_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token_hash BINARY(32) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_password_reset_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V18__create_customer_engagement_tables - structure
-- =============================================================================

CREATE TABLE loyalty_accounts (user_id BINARY(16) NOT NULL, coin_balance BIGINT NOT NULL DEFAULT 0, last_check_in DATE NULL, updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), PRIMARY KEY(user_id), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, CHECK(coin_balance>=0));

CREATE TABLE loyalty_transactions (id BIGINT NOT NULL AUTO_INCREMENT, user_id BINARY(16) NOT NULL, amount BIGINT NOT NULL, transaction_type VARCHAR(30) NOT NULL, description VARCHAR(255) NOT NULL, created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), PRIMARY KEY(id), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, INDEX idx_loyalty_tx_user(user_id,created_at));

CREATE TABLE tracking_events (id BIGINT NOT NULL AUTO_INCREMENT, order_id BINARY(16) NOT NULL, status VARCHAR(50) NOT NULL, description VARCHAR(500) NOT NULL, location VARCHAR(255) NULL, occurred_at DATETIME(6) NOT NULL, created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), PRIMARY KEY(id), FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE, INDEX idx_tracking_order_time(order_id,occurred_at));

CREATE TABLE newsletter_subscriptions (id BINARY(16) NOT NULL, email VARCHAR(255) NOT NULL, is_active BOOLEAN NOT NULL DEFAULT TRUE, created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), PRIMARY KEY(id), UNIQUE KEY uk_newsletter_email(email));

CREATE TABLE payment_methods (id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, provider VARCHAR(50) NOT NULL, provider_token VARCHAR(255) NOT NULL, display_name VARCHAR(100) NOT NULL, last_four VARCHAR(4) NULL, is_default BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), PRIMARY KEY(id), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, UNIQUE KEY uk_payment_provider_token(provider,provider_token), INDEX idx_payment_user(user_id));


-- =============================================================================
-- V19__order_idempotency_and_loyalty_ledger - structure
-- =============================================================================

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


-- =============================================================================
-- V20__create_customer_return_requests - structure
-- =============================================================================

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


-- =============================================================================
-- V21__secure_newsletter_consent - structure
-- =============================================================================

ALTER TABLE newsletter_subscriptions
    ADD COLUMN confirmation_token_hash BINARY(32) NULL AFTER email,
    ADD COLUMN unsubscribe_token_hash BINARY(32) NULL AFTER confirmation_token_hash,
    ADD COLUMN confirmed_at DATETIME(6) NULL AFTER is_active,
    ADD CONSTRAINT uk_newsletter_confirmation_token UNIQUE (confirmation_token_hash),
    ADD CONSTRAINT uk_newsletter_unsubscribe_token UNIQUE (unsubscribe_token_hash);


-- =============================================================================
-- V22__add_order_expiration - structure
-- =============================================================================

ALTER TABLE orders
    ADD COLUMN expires_at DATETIME(6) NULL AFTER payment_status,
    ADD INDEX idx_orders_pending_expiry (status, expires_at, id);


-- =============================================================================
-- V23__create_email_outbox - structure
-- =============================================================================

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


-- =============================================================================
-- V24__create_brand_settings - structure
-- =============================================================================

CREATE TABLE brand_settings (
    id SMALLINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    tagline VARCHAR(255) NOT NULL,
    story TEXT NOT NULL,
    founded INT NOT NULL,
    hotline VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_brand_settings_singleton CHECK (id = 1),
    CONSTRAINT chk_brand_settings_founded CHECK (founded BETWEEN 1800 AND 9999)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V25__expire_newsletter_confirmation_tokens - structure
-- =============================================================================

ALTER TABLE newsletter_subscriptions
    ADD COLUMN confirmation_expires_at DATETIME(6) NULL AFTER confirmation_token_hash;

CREATE INDEX idx_newsletter_confirmation_expiry
    ON newsletter_subscriptions (confirmation_expires_at);


-- =============================================================================
-- V26__add_email_verification - structure
-- =============================================================================

ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE AFTER is_active,
    ADD CONSTRAINT chk_users_email_verified CHECK (email_verified IN (0, 1));

CREATE TABLE email_verification_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token_hash BINARY(32) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_email_verification_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_email_verification_expiry CHECK (expires_at > created_at),
    INDEX idx_email_verification_user_created (user_id, created_at),
    INDEX idx_email_verification_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V27__stage_return_evidence_uploads - structure
-- =============================================================================

CREATE TABLE return_evidence_uploads (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    url VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    consumed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_return_evidence_upload_url UNIQUE (url),
    CONSTRAINT fk_return_evidence_upload_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_return_evidence_upload_expiry CHECK (expires_at > created_at),
    INDEX idx_return_evidence_upload_cleanup (consumed_at, expires_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
-- V29__admin_returns_refunds - structure
-- =============================================================================

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


-- =============================================================================
-- V30__inventory_adjustment_history - structure
-- =============================================================================

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


-- =============================================================================
-- V33__loyalty_return_debt - structure
-- =============================================================================

ALTER TABLE loyalty_accounts
    ADD COLUMN coin_debt BIGINT NOT NULL DEFAULT 0 AFTER coin_balance,
    ADD CONSTRAINT chk_loyalty_coin_debt CHECK (coin_debt >= 0);


-- =============================================================================
-- V34__admin_audit_logs - structure
-- =============================================================================

CREATE TABLE admin_audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_id BINARY(16) NOT NULL,
    method VARCHAR(10) NOT NULL,
    path VARCHAR(500) NOT NULL,
    response_status INT NOT NULL,
    ip_address VARCHAR(64) NULL,
    user_agent VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_admin_audit_created (created_at, id),
    KEY idx_admin_audit_admin_created (admin_id, created_at),
    CONSTRAINT fk_admin_audit_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
