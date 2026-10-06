ALTER TABLE reviews
    ADD COLUMN moderation_status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' AFTER comment,
    ADD COLUMN moderation_note VARCHAR(500) NULL AFTER moderation_status,
    ADD COLUMN moderated_by BINARY(16) NULL AFTER moderation_note,
    ADD COLUMN moderated_at DATETIME(6) NULL AFTER moderated_by,
    ADD KEY idx_reviews_moderation_created (moderation_status, created_at),
    ADD CONSTRAINT fk_reviews_moderated_by FOREIGN KEY (moderated_by) REFERENCES users(id) ON DELETE SET NULL;
