package com.lyrashop.catalog.variant.entity;

import static org.hibernate.type.SqlTypes.BINARY;
import static org.hibernate.type.SqlTypes.DECIMAL;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
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
import jakarta.persistence.Version;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(BINARY)
    @Column(name = "id", nullable = false, updatable = false, length = 16)
    private UUID id;

    @JdbcTypeCode(BINARY)
    @Column(name = "product_id", nullable = false, updatable = false, length = 16)
    private UUID productId;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "size", nullable = false, length = 20)
    private String size;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean")
    private boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private Instant updatedAt;

    protected ProductVariant() {
    }

    private ProductVariant(UUID productId, String sku, String size, String color, BigDecimal price, int stock) {
        this.productId = requireId(productId);
        this.sku = normalizeSku(sku);
        this.size = normalizeText(size, "size", 20);
        this.color = normalizeText(color, "color", 50);
        this.price = normalizePrice(price);
        if (stock < 0) throw new IllegalArgumentException("stock must be non-negative");
        this.stock = stock;
        this.active = true;
    }

    public static ProductVariant create(
            UUID productId, String sku, String size, String color, BigDecimal price, int stock
    ) {
        return new ProductVariant(productId, sku, size, color, price, stock);
    }

    public void updateCatalog(String sku, String size, String color, BigDecimal price) {
        this.sku = normalizeSku(sku);
        this.size = normalizeText(size, "size", 20);
        this.color = normalizeText(color, "color", 50);
        this.price = normalizePrice(price);
    }

    public void adjustInventory(int stock) {
        if (stock < 0) throw new IllegalArgumentException("stock must be non-negative");
        this.stock = stock;
    }

    public void decrementStock(int quantity) {
        if (quantity < 1 || quantity > stock) {
            throw new IllegalArgumentException("quantity exceeds stock");
        }
        this.stock -= quantity;
    }

    public void incrementStock(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        this.stock += quantity;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    private static UUID requireId(UUID value) {
        if (value == null) throw new IllegalArgumentException("productId is required");
        return value;
    }

    private static String normalizeSku(String value) {
        String normalized = normalizeText(value, "sku", 100).toUpperCase(Locale.ROOT);
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("sku must not exceed 100 characters");
        }
        if (!normalized.matches("[A-Z0-9]+(?:-[A-Z0-9]+)*")) {
            throw new IllegalArgumentException("sku must contain letters, digits, and hyphens");
        }
        return normalized;
    }

    private static String normalizeText(String value, String field, int max) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        String normalized = value.strip();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        if (normalized.length() > max) throw new IllegalArgumentException(field + " must not exceed " + max + " characters");
        return normalized;
    }

    private static BigDecimal normalizePrice(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.scale() < 0 || value.scale() > 2
                || value.precision() - value.scale() > 10 || value.precision() > 12) {
            throw new IllegalArgumentException("price must be a non-negative amount with at most two decimals");
        }
        return value;
    }
}
