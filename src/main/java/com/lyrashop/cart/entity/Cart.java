package com.lyrashop.cart.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(BINARY)
    @Column(name = "id", nullable = false, updatable = false, length = 16)
    private UUID id;

    @JdbcTypeCode(BINARY)
    @Column(name = "user_id", nullable = false, updatable = false, length = 16)
    private UUID userId;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private Instant updatedAt;

    protected Cart() {
    }

    private Cart(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        this.userId = userId;
    }

    public static Cart create(UUID userId) {
        return new Cart(userId);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
