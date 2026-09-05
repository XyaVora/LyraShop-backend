-- =============================================================================
--  LyraShop Database Schema
--  Kết hợp từ các Flyway migrations: V1 → V4
--
--  Cách import:
--    mysql -u <user> -p < lyrashop_schema.sql
--  Hoặc trong MySQL Workbench / DBeaver: File → Run SQL Script
--
--  Yêu cầu: MySQL 8.0+
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
--  Tạo user MySQL cho ứng dụng (tuỳ chọn — bỏ comment nếu cần)
--  Thay thế 'your_password' bằng mật khẩu thực
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
