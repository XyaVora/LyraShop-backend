ALTER TABLE orders
    ADD COLUMN paid_at DATETIME(6) NULL AFTER payment_status,
    ADD INDEX idx_orders_paid_at (paid_at);

UPDATE orders
SET paid_at = COALESCE(delivered_at, updated_at, created_at)
WHERE payment_status IN ('PAID', 'REFUNDED')
  AND paid_at IS NULL;
