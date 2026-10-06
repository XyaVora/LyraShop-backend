ALTER TABLE users
    DROP CHECK chk_users_role,
    ADD CONSTRAINT chk_users_role CHECK (
        role IN ('CUSTOMER', 'ADMIN', 'CATALOG_MANAGER', 'ORDER_MANAGER', 'SUPPORT')
    );
