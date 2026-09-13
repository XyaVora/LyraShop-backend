-- =============================================================================
--  LyraShop Database Schema
--  Standalone schema snapshot aligned with Flyway migrations V1-V12.
--
--  Source of truth for the running API:
--    src/main/resources/db/migration
--  Local Docker MySQL (scripts/local-up.ps1) is migrated by Flyway on startup.
--  Do not import this file into that database; CREATE TABLE would then collide
--  with Flyway migrations.
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
        CHECK (payment_method IN ('COD', 'VNPAY')),
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
--  V11 -- Wishlists, shipping addresses, and promotions
-- =============================================================================

CREATE TABLE IF NOT EXISTS wishlist_items (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BINARY(16)  NOT NULL,
    product_id  BINARY(16)  NOT NULL,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_wishlist_items
        PRIMARY KEY (id),
    CONSTRAINT uk_wishlist_items_user_product
        UNIQUE (user_id, product_id),
    CONSTRAINT fk_wishlist_items_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_items_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,

    INDEX idx_wishlist_items_user_created (user_id, created_at, id)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS shipping_addresses (
    id              BINARY(16)    NOT NULL,
    user_id         BINARY(16)    NOT NULL,
    recipient_name  VARCHAR(255)  NOT NULL,
    phone           VARCHAR(20)   NOT NULL,
    address_line    VARCHAR(500)  NOT NULL,
    ward            VARCHAR(255)  NULL,
    district        VARCHAR(255)  NOT NULL,
    city            VARCHAR(255)  NOT NULL,
    is_default      BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_shipping_addresses
        PRIMARY KEY (id),
    CONSTRAINT fk_shipping_addresses_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_shipping_addresses_recipient
        CHECK (CHAR_LENGTH(TRIM(recipient_name)) > 0),
    CONSTRAINT chk_shipping_addresses_phone
        CHECK (CHAR_LENGTH(TRIM(phone)) > 0),
    CONSTRAINT chk_shipping_addresses_line
        CHECK (CHAR_LENGTH(TRIM(address_line)) > 0),
    CONSTRAINT chk_shipping_addresses_district
        CHECK (CHAR_LENGTH(TRIM(district)) > 0),
    CONSTRAINT chk_shipping_addresses_city
        CHECK (CHAR_LENGTH(TRIM(city)) > 0),
    CONSTRAINT chk_shipping_addresses_default
        CHECK (is_default IN (0, 1)),

    INDEX idx_shipping_addresses_user_default (user_id, is_default, created_at)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS promotions (
    id           BINARY(16)     NOT NULL,
    name         VARCHAR(255)   NOT NULL,
    description  TEXT           NULL,
    discount_percent INT        NOT NULL,
    start_at     DATETIME(6)    NOT NULL,
    end_at       DATETIME(6)    NOT NULL,
    is_active    BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_promotions
        PRIMARY KEY (id),
    CONSTRAINT chk_promotions_name
        CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_promotions_discount
        CHECK (discount_percent BETWEEN 0 AND 100),
    CONSTRAINT chk_promotions_dates
        CHECK (end_at > start_at),
    CONSTRAINT chk_promotions_active
        CHECK (is_active IN (0, 1)),

    INDEX idx_promotions_active_window (is_active, start_at, end_at)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS promotion_products (
    promotion_id      BINARY(16)      NOT NULL,
    product_id        BINARY(16)      NOT NULL,
    sale_price        DECIMAL(12, 2)  NOT NULL,
    original_price    DECIMAL(12, 2)  NOT NULL,
    discount_percent  INT             NOT NULL,
    CONSTRAINT pk_promotion_products
        PRIMARY KEY (promotion_id, product_id),
    CONSTRAINT fk_promotion_products_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotions (id) ON DELETE CASCADE,
    CONSTRAINT fk_promotion_products_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT chk_promotion_products_prices
        CHECK (sale_price >= 0 AND original_price >= sale_price),
    CONSTRAINT chk_promotion_products_discount
        CHECK (discount_percent BETWEEN 0 AND 100),

    INDEX idx_promotion_products_product (product_id)

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
--  Dữ liệu mẫu (Sample Seed Data)
-- =============================================================================

-- 1. Users
-- Mật khẩu cho Admin: AdminPass1234
-- Mật khẩu cho Khách hàng: Password1234!
INSERT IGNORE INTO users (id, email, password, full_name, phone, role, is_active, version, created_at, updated_at) VALUES
(UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), 'admin@lyrashop.local', '{bcrypt}$2b$12$FxIb3nc1ebB36wD.CbfZLOnIzlgloF8TBAZs6zHEJKJNfoa5.uGAW', 'Quản trị viên Lyra', '0988000001', 'ADMIN', 1, 0, NOW(), NOW()),
(UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 'user@lyrashop.local', '{bcrypt}$2b$12$GT2ykjHZu7kHDHo31iDHbOSXurzRBlQhj9zWux1LnP2JjJidJhZ8K', 'Nguyễn Văn An', '0912345678', 'CUSTOMER', 1, 0, NOW(), NOW()),
(UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 'hoangmai@gmail.com', '{bcrypt}$2b$12$GT2ykjHZu7kHDHo31iDHbOSXurzRBlQhj9zWux1LnP2JjJidJhZ8K', 'Trần Thị Hoàng Mai', '0987654321', 'CUSTOMER', 1, 0, NOW(), NOW()),
(UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 'leminh@gmail.com', '{bcrypt}$2b$12$GT2ykjHZu7kHDHo31iDHbOSXurzRBlQhj9zWux1LnP2JjJidJhZ8K', 'Lê Minh Tuấn', '0909112233', 'CUSTOMER', 1, 0, NOW(), NOW());

-- 2. Categories
INSERT IGNORE INTO categories (id, name, slug, description, parent_id, is_active, created_at, updated_at) VALUES
(1, 'Thời trang nữ', 'thoi-trang-nu', 'Váy đầm, áo kiểu, quần tây và trang phục cao cấp dành cho phái đẹp.', NULL, 1, NOW(), NOW()),
(2, 'Thời trang nam', 'thoi-trang-nam', 'Áo sơ mi, áo polo, vest và phong cách thời trang lịch lãm cho nam giới.', NULL, 1, NOW(), NOW()),
(3, 'Giày dép', 'giay-dep', 'Giày cao gót, giày da Oxford, sneaker và giày lười phong cách.', NULL, 1, NOW(), NOW()),
(4, 'Phụ kiện', 'phu-kien', 'Túi xách da, thắt lưng, khăn lụa và trang sức tinh tế.', NULL, 1, NOW(), NOW());

-- 3. Products
INSERT IGNORE INTO products (id, category_id, name, slug, description, base_price, is_active, version, created_at, updated_at) VALUES
-- Category 1: Nữ
(UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 1, 'Đầm Lụa Satin Cổ V Dáng Dài', 'dam-lua-satin-co-v-dang-dai', 'Chất liệu lụa satin cao cấp bóng nhẹ, phom dáng dài thướt tha tôn vinh nét kiêu sa nữ tính. Thích hợp cho dạ tiệc và sự kiện sang trọng.', 850000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 1, 'Áo Sơ Mi Lụa Tơ Tằm Thêu Tay', 'ao-so-mi-lua-to-tam-theu-tay', 'Chi tiết thêu hoa thủ công tỉ mỉ trên nền lụa tơ tằm mềm mại, thoáng mát và thanh lịch cho quý cô công sở.', 650000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 1, 'Chân Váy Xếp Ly Dáng Xòe Midi', 'chan-vay-xep-ly-dang-xoe-midi', 'Đường xếp ly sắc nét, độ rủ mềm mại giúp từng bước chân thêm uyển chuyển. Dễ dàng phối cùng sơ mi hoặc áo len mỏng.', 520000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 1, 'Áo Blazer Nữ Dáng Suông Công Sở', 'ao-blazer-nu-dang-suong-cong-so', 'Thiết kế vai đệm nhẹ tạo phom chuẩn mực, đường may tinh xảo đem lại diện mạo chuyên nghiệp và cuốn hút.', 1150000.00, 1, 0, NOW(), NOW()),

-- Category 2: Nam
(UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 2, 'Áo Sơ Mi Nam Oxford Trắng Cao Cấp', 'ao-so-mi-nam-oxford-trang-cao-cap', 'Dệt từ 100% sợi bông cotton chải kỹ, bề mặt dệt Oxford đứng phom và ít nhăn, tiêu chuẩn cho phong cách lịch lãm hàng ngày.', 590000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 2, 'Áo Polo Nam Dệt Kim Thoáng Khí', 'ao-polo-nam-det-kim-thoang-khi', 'Cấu trúc dệt kim thông thoáng, thấm hút mồ hôi tốt. Bo cổ dệt tinh tế mang lại vẻ ngoài năng động nhưng lịch thiệp.', 450000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 2, 'Quần Tây Nam Dáng Slimfit Co Giãn', 'quan-tay-nam-dang-slimfit-co-gian', 'Chất vải wool pha co giãn nhẹ, đường ly ép vĩnh viễn giúp tôn dáng đôi chân và tạo sự thoải mái suốt ngày dài.', 680000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 2, 'Áo Khoác Măng Tô Nam Dạ Wool', 'ao-khoac-mang-to-nam-da-wool', 'Dạ ép lông cừu giữ nhiệt vượt trội, phom dài chuẩn phong cách quý ông cổ điển phương Tây.', 1850000.00, 1, 0, NOW(), NOW()),

-- Category 3: Giày dép
(UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 3, 'Giày Cao Gót Mũi Nhọn Da Bóng 7cm', 'giay-cao-got-mui-nhon-da-bong-7cm', 'Da bóng sang trọng, gót nhọn thanh mảnh với đế đệm êm ái nâng đỡ bàn chân tối ưu khi di chuyển.', 790000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 3, 'Giày Da Oxford Nam Da Bò Ý', 'giay-da-oxford-nam-da-bo-y', 'Da bò nguyên tấm nhập khẩu Ý, đánh xi bóng thủ công patina đẳng cấp dành cho những dịp trang trọng.', 1650000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 3, 'Giày Loafer Da Lộn Phong Cách Ý', 'giay-loafer-da-lon-phong-cach-y', 'Da lộn tự nhiên mềm mịn, phom giày không dây tiện lợi, thoải mái cho những buổi dạo phố cuối tuần.', 950000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 3, 'Giày Sneaker Da Tối Giản Unisex', 'giay-sneaker-da-toi-gian-unisex', 'Thiết kế trắng tinh khôi theo xu hướng tối giản Minimalist, đế cao su nguyên khối bền bỉ và êm nhẹ.', 720000.00, 1, 0, NOW(), NOW()),

-- Category 4: Phụ kiện
(UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), 4, 'Túi Xách Da Thật Quai Ngọc Trai', 'tui-xach-da-that-quai-ngoc-trai', 'Điểm nhấn quai xách ngọc trai nhân tạo quý phái kết hợp thân túi da bò dập vân tinh tế.', 1450000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 4, 'Thắt Lưng Da Bò Khóa Kim Cổ Điển', 'that-lung-da-bo-khoa-kim-co-dien', 'Mặt khóa kim loại nguyên khối mạ titan chống gỉ, dây da bò 2 lớp bền bỉ theo năm tháng.', 390000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000015'), 4, 'Khăn Lụa Vuông Họa Tiết Baroque', 'khan-lua-vuong-hoa-tiet-baroque', 'Họa tiết cổ điển in kỹ thuật số sắc nét trên nền lụa 100%, viền khăn cuốn mép thủ công tỉ mỉ.', 320000.00, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000016'), 4, 'Ví Da Cầm Tay Nam Khóa Số', 'vi-da-cam-tay-nam-khoa-so', 'Ví clutch nam tiện dụng với nhiều ngăn đựng điện thoại, thẻ và tiền mặt kèm khóa số an toàn.', 890000.00, 1, 0, NOW(), NOW());

-- 4. Product Variants
INSERT IGNORE INTO product_variants (id, product_id, sku, size, color, price, stock, is_active, version, created_at, updated_at) VALUES
-- SP 1 (Đầm lụa satin)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-S-DEN', 'S', 'Đen', 850000.00, 25, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-M-DEN', 'M', 'Đen', 850000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000103'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'DL-SATIN-M-BE', 'M', 'Be', 850000.00, 20, 1, 0, NOW(), NOW()),

-- SP 2 (Áo sơ mi tơ tằm)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-S-TRANG', 'S', 'Trắng', 650000.00, 40, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-M-TRANG', 'M', 'Trắng', 650000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000203'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'SM-TOTAM-L-HONG', 'L', 'Hồng Pastel', 650000.00, 15, 1, 0, NOW(), NOW()),

-- SP 3 (Chân váy xếp ly)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 'CV-STEPLY-S-NAU', 'S', 'Nâu Tây', 520000.00, 20, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000302'), UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), 'CV-STEPLY-M-DEN', 'M', 'Đen', 520000.00, 45, 1, 0, NOW(), NOW()),

-- SP 4 (Blazer nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-S-KEM', 'S', 'Kem', 1150000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000402'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), 'BZ-NU-M-DEN', 'M', 'Đen', 1150000.00, 20, 1, 0, NOW(), NOW()),

-- SP 5 (Sơ mi Oxford nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'SM-OXFORD-M-TRANG', 'M', 'Trắng', 590000.00, 50, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000502'), UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), 'SM-OXFORD-L-XANH', 'L', 'Xanh Nhạt', 590000.00, 40, 1, 0, NOW(), NOW()),

-- SP 6 (Polo nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 'PL-KNIT-M-NAVY', 'M', 'Xanh Navy', 450000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000602'), UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), 'PL-KNIT-L-XAM', 'L', 'Xám Tiêu', 450000.00, 25, 1, 0, NOW(), NOW()),

-- SP 7 (Quần tây nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000701'), UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 'QT-SLIM-30-DEN', '30', 'Đen', 680000.00, 35, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000702'), UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), 'QT-SLIM-32-XAM', '32', 'Xám Đậm', 680000.00, 25, 1, 0, NOW(), NOW()),

-- SP 8 (Măng tô nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000801'), UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 'MT-WOOL-L-CAMEL', 'L', 'Camel', 1850000.00, 12, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000802'), UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), 'MT-WOOL-XL-DEN', 'XL', 'Đen', 1850000.00, 10, 1, 0, NOW(), NOW()),

-- SP 9 (Cao gót nữ)
(UUID_TO_BIN('30000000-0000-0000-0000-000000000901'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-36-NUDE', '36', 'Nude', 790000.00, 18, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000000902'), UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), 'CG-PUMP-37-DEN', '37', 'Đen', 790000.00, 22, 1, 0, NOW(), NOW()),

-- SP 10 (Oxford nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-40-NAU', '40', 'Nâu Cổ Điển', 1650000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), 'OX-ITALY-41-DEN', '41', 'Đen', 1650000.00, 20, 1, 0, NOW(), NOW()),

-- SP 11 (Loafer)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001101'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-40-BO', '40', 'Vàng Bò', 950000.00, 15, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001102'), UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), 'LF-SUEDE-41-NAVY', '41', 'Navy', 950000.00, 18, 1, 0, NOW(), NOW()),

-- SP 12 (Sneaker)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001201'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-38-TRANG', '38', 'Trắng', 720000.00, 30, 1, 0, NOW(), NOW()),
(UUID_TO_BIN('30000000-0000-0000-0000-000000001202'), UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), 'SNK-MINI-41-TRANG', '41', 'Trắng', 720000.00, 40, 1, 0, NOW(), NOW()),

-- SP 13 (Túi xách ngọc trai)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001301'), UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), 'BAG-PEARL-BE', 'Freesize', 'Be Nhạt', 1450000.00, 15, 1, 0, NOW(), NOW()),

-- SP 14 (Thắt lưng nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001401'), UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), 'BELT-LEATHER-DEN', '115cm', 'Đen', 390000.00, 50, 1, 0, NOW(), NOW()),

-- SP 15 (Khăn lụa)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001501'), UUID_TO_BIN('20000000-0000-0000-0000-000000000015'), 'SCARF-SILK-70', '70x70cm', 'Đa sắc', 320000.00, 60, 1, 0, NOW(), NOW()),

-- SP 16 (Ví clutch nam)
(UUID_TO_BIN('30000000-0000-0000-0000-000000001601'), UUID_TO_BIN('20000000-0000-0000-0000-000000000016'), 'CLUTCH-LOCK-DEN', '28x18cm', 'Đen', 890000.00, 20, 1, 0, NOW(), NOW());

-- 5. Product Images
INSERT IGNORE INTO product_images (product_id, variant_id, url, sort_order, is_primary, created_at) VALUES
(UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), NULL, 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), NULL, 'https://images.unsplash.com/photo-1598554747436-c9293d6a588f?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), NULL, 'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), NULL, 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), NULL, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000006'), NULL, 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000007'), NULL, 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000008'), NULL, 'https://images.unsplash.com/photo-1544923246-77307dd654cb?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000009'), NULL, 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), NULL, 'https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000011'), NULL, 'https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000012'), NULL, 'https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), NULL, 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000014'), NULL, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000015'), NULL, 'https://images.unsplash.com/photo-1601924994987-69e26d50dc26?w=800', 0, 1, NOW()),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000016'), NULL, 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=800', 0, 1, NOW());

-- 6. Reviews
INSERT IGNORE INTO reviews (product_id, user_id, rating, comment, created_at) VALUES
(UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 5, 'Váy mặc rất tôn dáng, lụa mềm mượt và bóng nhẹ rất sang. Giao hàng cực nhanh!', NOW() - INTERVAL 5 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Chất vải mát, đường may chuẩn chỉ. Mình mặc dự tiệc cưới ai cũng khen.', NOW() - INTERVAL 2 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 5, 'Áo thêu tay rất tỉ mỉ, chất tơ tằm mặc nhẹ như không. Rất ưng ý!', NOW() - INTERVAL 4 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000005'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), 5, 'Áo sơ mi Oxford phom cực đẹp, vải dày dặn mà không hề bí. Đáng tiền!', NOW() - INTERVAL 3 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000010'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 5, 'Da bò xịn, đi êm chân không bị đau gót. Đóng gói hộp rất cao cấp.', NOW() - INTERVAL 1 DAY),
(UUID_TO_BIN('20000000-0000-0000-0000-000000000013'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 4, 'Túi xinh xắn, quai ngọc trai tạo điểm nhấn rất đẹp. Đựng vừa điện thoại và son phấn.', NOW() - INTERVAL 6 DAY);

-- 7. Sample Orders
INSERT IGNORE INTO orders (id, user_id, total_amount, status, payment_method, payment_status, shipping_address, shipping_phone, note, created_at, updated_at) VALUES
(UUID_TO_BIN('40000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 1440000.00, 'DELIVERED', 'COD', 'PAID', '123 Phố Huế, Phường Bùi Thị Xuân, Quận Hai Bà Trưng, Hà Nội', '0912345678', 'Giao giờ hành chính', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 5 DAY),
(UUID_TO_BIN('40000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'), 850000.00, 'SHIPPING', 'COD', 'UNPAID', '45 Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', '0987654321', 'Gọi trước khi giao', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(UUID_TO_BIN('40000000-0000-0000-0000-000000000003'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 1150000.00, 'PENDING', 'COD', 'UNPAID', '123 Phố Huế, Phường Bùi Thị Xuân, Quận Hai Bà Trưng, Hà Nội', '0912345678', NULL, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR);

-- 8. Order Items
INSERT IGNORE INTO order_items (id, order_id, variant_id, product_name, sku, size, color, quantity, unit_price, subtotal) VALUES
(1, UUID_TO_BIN('40000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000101'), 'Đầm Lụa Satin Cổ V Dáng Dài', 'DL-SATIN-S-DEN', 'S', 'Đen', 1, 850000.00, 850000.00),
(2, UUID_TO_BIN('40000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000501'), 'Áo Sơ Mi Nam Oxford Trắng Cao Cấp', 'SM-OXFORD-M-TRANG', 'M', 'Trắng', 1, 590000.00, 590000.00),
(3, UUID_TO_BIN('40000000-0000-0000-0000-000000000002'), UUID_TO_BIN('30000000-0000-0000-0000-000000000102'), 'Đầm Lụa Satin Cổ V Dáng Dài', 'DL-SATIN-M-DEN', 'M', 'Đen', 1, 850000.00, 850000.00),
(4, UUID_TO_BIN('40000000-0000-0000-0000-000000000003'), UUID_TO_BIN('30000000-0000-0000-0000-000000000401'), 'Áo Blazer Nữ Dáng Suông Công Sở', 'BZ-NU-S-KEM', 'S', 'Kem', 1, 1150000.00, 1150000.00);

-- 9. Sample Cart
INSERT IGNORE INTO carts (id, user_id, created_at, updated_at) VALUES
(UUID_TO_BIN('50000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), NOW(), NOW());

INSERT IGNORE INTO cart_items (id, cart_id, variant_id, quantity, created_at, updated_at) VALUES
(1, UUID_TO_BIN('50000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000601'), 2, NOW(), NOW());


-- 10. Active promotion
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
