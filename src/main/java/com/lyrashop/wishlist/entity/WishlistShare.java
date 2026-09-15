package com.lyrashop.wishlist.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import static org.hibernate.type.SqlTypes.BINARY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wishlist_shares")
public class WishlistShare {
    @Id @JdbcTypeCode(BINARY) @Column(length = 16)
    private UUID id;
    @JdbcTypeCode(BINARY) @Column(name = "user_id", nullable = false, updatable = false, length = 16)
    private UUID userId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    protected WishlistShare() {}
    public static WishlistShare create(UUID userId) {
        var share = new WishlistShare();
        share.id = UUID.randomUUID(); share.userId = userId;
        share.createdAt = Instant.now(); share.expiresAt = share.createdAt.plusSeconds(30L * 24 * 60 * 60);
        return share;
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Instant getExpiresAt() { return expiresAt; }
}
