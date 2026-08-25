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
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @JdbcTypeCode(BINARY)
    @Column(name = "cart_id", nullable = false, updatable = false, length = 16)
    private UUID cartId;

    @JdbcTypeCode(BINARY)
    @Column(name = "variant_id", nullable = false, updatable = false, length = 16)
    private UUID variantId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private Instant updatedAt;

    protected CartItem() {
    }

    private CartItem(UUID cartId, UUID variantId, int quantity) {
        if (cartId == null) throw new IllegalArgumentException("cartId is required");
        if (variantId == null) throw new IllegalArgumentException("variantId is required");
        this.cartId = cartId;
        this.variantId = variantId;
        setQuantity(quantity);
    }

    public static CartItem create(UUID cartId, UUID variantId, int quantity) {
        return new CartItem(cartId, variantId, quantity);
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("quantity must be at least 1");
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public UUID getCartId() { return cartId; }
    public UUID getVariantId() { return variantId; }
    public int getQuantity() { return quantity; }
}
