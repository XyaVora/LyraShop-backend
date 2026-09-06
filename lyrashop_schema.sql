-- =============================================================================
--  LyraShop Database Schema
--  Readable snapshot of Flyway migrations V1-V8.
--
--  Source of truth for the running API:
--    src/main/resources/db/migration
--  Local Docker MySQL (scripts/local-up.ps1) is migrated by Flyway on startup.
--  Do not import this file into that database; CREATE TABLE would then collide
--  with Flyway V1-V8.
--
--  Workbench / DBeaver: File -> Run SQL Script against an empty inspection DB.
--  Require MySQL 8.0+.
-- =============================================================================

-- Tạo database nếu chưa có
CREATE DATABASE IF NOT EXISTS lyrashop_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE lyrashop_db;

-- =============================================================================
--  V1 — Identity tables: users + refresh_sessions
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id          BINARY(16)      NOT NULL,
    email       VARCHAR(255)    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL,
    password    VARCHAR(255)    CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    full_name   VARCHAR(255)    NOT NULL,
    phone       VARCHAR(20)     NULL,
    role        VARCHAR(20)     CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'CUSTOMER',
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    version     BIGINT          NOT NULL DEFAULT 0,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_users
        PRIMARY KEY (id),
    CONSTRAINT uk_users_email
        UNIQUE (email),
    CONSTRAINT chk_users_email_not_blank
        CHECK (CHAR_LENGTH(TRIM(email)) > 0),
    CONSTRAINT chk_users_password_not_blank
        CHECK (CHAR_LENGTH(password) > 0),
    CONSTRAINT chk_users_full_name_not_blank
        CHECK (CHAR_LENGTH(TRIM(full_name)) > 0),
    CONSTRAINT chk_users_role
        CHECK (role IN ('CUSTOMER', 'ADMIN')),
    CONSTRAINT chk_users_active
        CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_users_version
        CHECK (version >= 0)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS refresh_sessions (
    id          BINARY(16)      NOT NULL,
    user_id     BINARY(16)      NOT NULL,
    family_id   BINARY(16)      NOT NULL,
    token_hash  BINARY(32)      NOT NULL,   -- SHA-256 của raw token
    expires_at  DATETIME(6)     NOT NULL,
    consumed_at DATETIME(6)     NULL,
    revoked_at  DATETIME(6)     NULL,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version     BIGINT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_refresh_sessions
        PRIMARY KEY (id),
    CONSTRAINT uk_refresh_sessions_token_hash
        UNIQUE (token_hash),
    CONSTRAINT fk_refresh_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_refresh_sessions_expiry
        CHECK (expires_at > created_at),
    CONSTRAINT chk_refresh_sessions_consumed
        CHECK (consumed_at IS NULL OR consumed_at >= created_at),
    CONSTRAINT chk_refresh_sessions_revoked
        CHECK (revoked_at IS NULL OR revoked_at >= created_at),
    CONSTRAINT chk_refresh_sessions_version
        CHECK (version >= 0),

    INDEX idx_refresh_sessions_user_state   (user_id,   revoked_at),
    INDEX idx_refresh_sessions_family_state (family_id, revoked_at),
    INDEX idx_refresh_sessions_expiry       (expires_at)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
--  V2 — Catalog: categories
-- =============================================================================

CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    slug        VARCHAR(100)    CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    description TEXT            NULL,
    parent_id   BIGINT          NULL,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_categories
        PRIMARY KEY (id),
    CONSTRAINT uk_categories_slug
        UNIQUE (slug),
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT chk_categories_name_not_blank
        CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_categories_slug_format
        CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT chk_categories_active
        CHECK (is_active IN (0, 1)),

    INDEX idx_categories_active_name (is_active, name, id)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
--  V3 — Catalog: products
-- =============================================================================

CREATE TABLE IF NOT EXISTS products (
    id          BINARY(16)      NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    slug        VARCHAR(255)    CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    description TEXT            NULL,
    base_price  DECIMAL(12, 2)  NOT NULL,
    category_id BIGINT          NOT NULL,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    version     BIGINT          NOT NULL DEFAULT 0,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_products
        PRIMARY KEY (id),
    CONSTRAINT uk_products_slug
        UNIQUE (slug),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT chk_products_name_not_blank
        CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_products_slug_format
        CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT chk_products_price
        CHECK (base_price >= 0),
    CONSTRAINT chk_products_active
        CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_products_version
        CHECK (version >= 0),

    INDEX idx_products_active_category (is_active, category_id, name, id),
    INDEX idx_products_active_price    (is_active, base_price,  id)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
--  V4 — Catalog: product_variants
-- =============================================================================

CREATE TABLE IF NOT EXISTS product_variants (
    id          BINARY(16)      NOT NULL,
    product_id  BINARY(16)      NOT NULL,
    sku         VARCHAR(100)    CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    size        VARCHAR(20)     NOT NULL,
    color       VARCHAR(50)     NOT NULL,
    price       DECIMAL(12, 2)  NOT NULL,
    stock       INT             NOT NULL DEFAULT 0,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    version     BIGINT          NOT NULL DEFAULT 0,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_product_variants
        PRIMARY KEY (id),
    CONSTRAINT uk_product_variants_sku
        UNIQUE (sku),
    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT chk_product_variants_sku_not_blank
        CHECK (CHAR_LENGTH(TRIM(sku)) > 0),
    CONSTRAINT chk_product_variants_size_not_blank
        CHECK (CHAR_LENGTH(TRIM(size)) > 0),
    CONSTRAINT chk_product_variants_color_not_blank
        CHECK (CHAR_LENGTH(TRIM(color)) > 0),
    CONSTRAINT chk_product_variants_price
        CHECK (price >= 0),
    CONSTRAINT chk_product_variants_stock
        CHECK (stock >= 0),
    CONSTRAINT chk_product_variants_active
        CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_product_variants_version
        CHECK (version >= 0),

    INDEX idx_product_variants_product_active (product_id, is_active, id),
    INDEX idx_product_variants_active_stock   (is_active,  stock,     id)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
--  V5 -- Catalog: product_images
-- =============================================================================

CREATE TABLE IF NOT EXISTS product_images (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    product_id  BINARY(16)      NOT NULL,
    variant_id  BINARY(16)      NULL,
    url         VARCHAR(2048)   NOT NULL,
    is_primary  BOOLEAN         NOT NULL DEFAULT FALSE,
    sort_order  INT             NOT NULL DEFAULT 0,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_product_images
        PRIMARY KEY (id),
    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_images_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE SET NULL,
    CONSTRAINT chk_product_images_url_not_blank
        CHECK (CHAR_LENGTH(TRIM(url)) > 0),
    CONSTRAINT chk_product_images_primary
        CHECK (is_primary IN (0, 1)),
    CONSTRAINT chk_product_images_sort_order
        CHECK (sort_order >= 0),

    INDEX idx_product_images_product_sort (product_id, sort_order, id),
    INDEX idx_product_images_variant      (variant_id, id)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
--  V6 -- Carts
-- =============================================================================

CREATE TABLE IF NOT EXISTS carts (
    id          BINARY(16)      NOT NULL,
    user_id     BINARY(16)      NOT NULL,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_carts
        PRIMARY KEY (id),
    CONSTRAINT uk_carts_user
        UNIQUE (user_id),
    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    cart_id     BINARY(16)      NOT NULL,
    variant_id  BINARY(16)      NOT NULL,
    quantity    INT             NOT NULL DEFAULT 1,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_cart_items
        PRIMARY KEY (id),
    CONSTRAINT uk_cart_items_cart_variant
        UNIQUE (cart_id, variant_id),
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE RESTRICT,
    CONSTRAINT chk_cart_items_quantity
        CHECK (quantity >= 1)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
--  V7 -- Orders
-- =============================================================================

CREATE TABLE IF NOT EXISTS orders (
    id                  BINARY(16)      NOT NULL,
    user_id             BINARY(16)      NOT NULL,
    total_amount        DECIMAL(12, 2)  NOT NULL,
    status              VARCHAR(30)     CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'PENDING',
    payment_method      VARCHAR(30)     CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    payment_status      VARCHAR(20)     CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'UNPAID',
    shipping_address    TEXT            NOT NULL,
    shipping_phone      VARCHAR(20)     NOT NULL,
    note                TEXT            NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_orders
        PRIMARY KEY (id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_orders_total
        CHECK (total_amount >= 0),
    CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED','CANCELLED')),
    CONSTRAINT chk_orders_payment_method
        CHECK (payment_method IN ('COD')),
    CONSTRAINT chk_orders_payment_status
        CHECK (payment_status IN ('UNPAID','PAID','FAILED','REFUNDED')),
    CONSTRAINT chk_orders_shipping_address
        CHECK (CHAR_LENGTH(TRIM(shipping_address)) > 0),
    CONSTRAINT chk_orders_shipping_phone
        CHECK (CHAR_LENGTH(TRIM(shipping_phone)) > 0),

    INDEX idx_orders_user_created     (user_id, created_at, id),
    INDEX idx_orders_status_created   (status, created_at, id)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS order_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    order_id        BINARY(16)      NOT NULL,
    variant_id      BINARY(16)      NOT NULL,
    product_name    VARCHAR(255)    NOT NULL,
    sku             VARCHAR(100)    NOT NULL,
    size            VARCHAR(20)     NOT NULL,
    color           VARCHAR(50)     NOT NULL,
    quantity        INT             NOT NULL,
    unit_price      DECIMAL(12, 2)  NOT NULL,
    subtotal        DECIMAL(12, 2)  NOT NULL,

    CONSTRAINT pk_order_items
        PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_items_quantity
        CHECK (quantity >= 1),
    CONSTRAINT chk_order_items_unit_price
        CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_subtotal
        CHECK (subtotal >= 0),

    INDEX idx_order_items_order (order_id, id)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
--  V8 -- Reviews
-- =============================================================================

CREATE TABLE IF NOT EXISTS reviews (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    product_id  BINARY(16)      NOT NULL,
    user_id     BINARY(16)      NOT NULL,
    rating      INT             NOT NULL,
    comment     TEXT            NULL,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_reviews
        PRIMARY KEY (id),
    CONSTRAINT uk_reviews_product_user
        UNIQUE (product_id, user_id),
    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_reviews_rating
        CHECK (rating BETWEEN 1 AND 5),

    INDEX idx_reviews_product_created (product_id, created_at, id)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


-- =============================================================================
--  Optional app user. Leave commented; local-up uses compose.local.yaml.
-- =============================================================================

-- CREATE USER IF NOT EXISTS 'lyrashop'@'%' IDENTIFIED BY 'your_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON lyrashop_db.* TO 'lyrashop'@'%';
-- FLUSH PRIVILEGES;


-- =============================================================================
--  Kiểm tra kết quả
-- =============================================================================

SELECT
    table_name,
    table_rows,
    engine,
    table_collation
FROM information_schema.tables
WHERE table_schema = 'lyrashop_db'
ORDER BY table_name;
