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

INSERT INTO vouchers (id, code, label, voucher_type, discount_type, discount_value,
    max_discount_amount, minimum_order_amount, starts_at, ends_at, total_usage_limit, per_user_limit)
VALUES
    (UNHEX('10000000000000000000000000000001'), 'LYRA10', 'Giảm 10% giá trị sản phẩm',
     'discount', 'PERCENT', 10, NULL, 0, '2026-01-01 00:00:00', '2030-12-31 23:59:59', NULL, 20),
    (UNHEX('10000000000000000000000000000002'), 'FREESHIP', 'Miễn phí vận chuyển',
     'shipping', 'FREESHIP', 0, NULL, 0, '2026-01-01 00:00:00', '2030-12-31 23:59:59', NULL, 20),
    (UNHEX('10000000000000000000000000000003'), 'LYRA50K', 'Giảm 50.000đ cho đơn từ 800.000đ',
     'discount', 'FIXED', 50000, NULL, 800000, '2026-01-01 00:00:00', '2030-12-31 23:59:59', NULL, 20);
