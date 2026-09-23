package com.lyrashop.auth.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.BINARY)
    @Column(nullable = false, updatable = false, length = 16) private UUID id;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "user_id", nullable = false, updatable = false, length = 16) private UUID userId;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "token_hash", nullable = false, updatable = false, length = 32) private byte[] tokenHash;
    @Column(name = "expires_at", nullable = false, updatable = false) private Instant expiresAt;
    @Column(name = "used_at") private Instant usedAt;
    @CreationTimestamp(source = SourceType.DB) @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected EmailVerificationToken() {}

    private EmailVerificationToken(UUID userId, byte[] tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash.clone();
        this.expiresAt = expiresAt;
    }

    public static EmailVerificationToken issue(UUID userId, byte[] tokenHash, Instant expiresAt) {
        return new EmailVerificationToken(userId, tokenHash, expiresAt);
    }

    public UUID getUserId() { return userId; }

    public void use() {
        if (usedAt != null || !expiresAt.isAfter(Instant.now())) throw new IllegalStateException("verification token is inactive");
        usedAt = Instant.now();
    }
}
