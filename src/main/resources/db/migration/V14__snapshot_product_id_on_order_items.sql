ALTER TABLE order_items
    ADD COLUMN product_id BINARY(16) NULL AFTER variant_id;

UPDATE order_items item
JOIN product_variants variant ON variant.id = item.variant_id
SET item.product_id = variant.product_id;

ALTER TABLE order_items
    MODIFY COLUMN product_id BINARY(16) NOT NULL,
    ADD CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    ADD INDEX idx_order_items_product (product_id, order_id);
