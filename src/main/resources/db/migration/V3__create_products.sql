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