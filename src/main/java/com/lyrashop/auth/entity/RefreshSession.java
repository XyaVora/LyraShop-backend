package com.lyrashop.auth.entity;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.type.SqlTypes;

import com.lyrashop.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "refresh_sessions")
public class RefreshSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @NotNull
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "family_id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID familyId;

    @NotNull
    @Size(min = RefreshTokenDigest.LENGTH, max = RefreshTokenDigest.LENGTH)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "token_hash", nullable = false, updatable = false, columnDefinition = "binary(32)")
    private byte[] tokenHash;

    @NotNull
    @Column(name = "expires_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant expiresAt;

    @Column(name = "consumed_at", columnDefinition = "datetime(6)")
    private Instant consumedAt;

    @Column(name = "revoked_at", columnDefinition = "datetime(6)")
    private Instant revokedAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected RefreshSession() {
    }

    private RefreshSession(User user, UUID familyId, RefreshTokenDigest tokenDigest, Instant expiresAt) {
        this.user = Objects.requireNonNull(user, "user is required");
        this.familyId = Objects.requireNonNull(familyId, "familyId is required");
        this.tokenHash = Objects.requireNonNull(tokenDigest, "tokenDigest is required").bytes();
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt is required");
    }

    public static RefreshSession issue(
            User user,
            UUID familyId,
            RefreshTokenDigest tokenDigest,
            Instant expiresAt
    ) {
        return new RefreshSession(user, familyId, tokenDigest, expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public byte[] getTokenHash() {
        return Arrays.copyOf(tokenHash, tokenHash.length);
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getVersion() {
        return version;
    }
}
