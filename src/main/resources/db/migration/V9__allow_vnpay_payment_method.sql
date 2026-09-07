ALTER TABLE orders DROP CHECK chk_orders_payment_method;
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_payment_method
        CHECK (payment_method IN ('COD', 'VNPAY'));
