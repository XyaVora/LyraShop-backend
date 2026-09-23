ALTER TABLE newsletter_subscriptions
    ADD COLUMN confirmation_token_hash BINARY(32) NULL AFTER email,
    ADD COLUMN unsubscribe_token_hash BINARY(32) NULL AFTER confirmation_token_hash,
    ADD COLUMN confirmed_at DATETIME(6) NULL AFTER is_active,
    ADD CONSTRAINT uk_newsletter_confirmation_token UNIQUE (confirmation_token_hash),
    ADD CONSTRAINT uk_newsletter_unsubscribe_token UNIQUE (unsubscribe_token_hash);

UPDATE newsletter_subscriptions SET confirmed_at = created_at WHERE is_active = TRUE;
