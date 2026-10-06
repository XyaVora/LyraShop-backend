ALTER TABLE inventory_adjustments
    MODIFY COLUMN admin_id BINARY(16) NULL,
    ADD COLUMN movement_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL' AFTER admin_id,
    ADD COLUMN order_id BINARY(16) NULL AFTER movement_type,
    ADD KEY idx_inventory_adjustment_order (order_id),
    ADD CONSTRAINT fk_inventory_adjustment_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT;
