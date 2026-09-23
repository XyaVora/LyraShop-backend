ALTER TABLE newsletter_subscriptions
    ADD COLUMN confirmation_expires_at DATETIME(6) NULL AFTER confirmation_token_hash;

UPDATE newsletter_subscriptions
SET confirmation_expires_at = DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 24 HOUR)
WHERE confirmation_token_hash IS NOT NULL;

CREATE INDEX idx_newsletter_confirmation_expiry
    ON newsletter_subscriptions (confirmation_expires_at);
