ALTER TABLE orders
    ADD COLUMN expires_at DATETIME(6) NULL AFTER payment_status,
    ADD INDEX idx_orders_pending_expiry (status, expires_at, id);

UPDATE orders
SET expires_at = CASE
    WHEN payment_method = 'VNPAY' AND payment_status = 'UNPAID'
        THEN DATE_ADD(created_at, INTERVAL 30 MINUTE)
    ELSE DATE_ADD(created_at, INTERVAL 24 HOUR)
END
WHERE status = 'PENDING';
