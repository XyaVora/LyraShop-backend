package com.lyrashop.wishlist.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(name = "wishlist_items")
public class WishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(BINARY)
    @Column(name = "user_id", nullable = false, updatable = false, length = 16)
    private UUID userId;

    @JdbcTypeCode(BINARY)
    @Column(name = "product_id", nullable = false, updatable = false, length = 16)
    private UUID productId;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    protected WishlistItem() {}

    private WishlistItem(UUID userId, UUID productId) {
        if (userId == null || productId == null) throw new IllegalArgumentException("userId and productId are required");
        this.userId = userId;
        this.productId = productId;
    }

    public static WishlistItem create(UUID userId, UUID productId) { return new WishlistItem(userId, productId); }
    public Long getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getProductId() { return productId; }
    public Instant getCreatedAt() { return createdAt; }
}
