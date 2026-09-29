ALTER TABLE loyalty_accounts
    ADD COLUMN coin_debt BIGINT NOT NULL DEFAULT 0 AFTER coin_balance,
    ADD CONSTRAINT chk_loyalty_coin_debt CHECK (coin_debt >= 0);
