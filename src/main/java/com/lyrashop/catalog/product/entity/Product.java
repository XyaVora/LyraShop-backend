package com.lyrashop.catalog.product.entity;

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

import static org.hibernate.type.SqlTypes.BINARY;
import static org.hibernate.type.SqlTypes.DECIMAL;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(BINARY)
    @Column(name = "id", nullable = false, updatable = false, length = 16)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean")
    private boolean active;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Product() {
    }

    private Product(
            String name,
            String slug,
            String description,
            BigDecimal basePrice,
            Long categoryId
    ) {
        this.name = normalizeText(name, "name", 255);
        this.slug = normalizeSlug(slug);
        this.description = normalizeNullable(description, 2_000);
        this.basePrice = normalizePrice(basePrice);
        this.categoryId = requirePositive(categoryId, "categoryId");
        this.active = true;
    }

    public static Product create(
            String name,
            String slug,
            String description,
            BigDecimal basePrice,
            Long categoryId
    ) {
        return new Product(name, slug, description, basePrice, categoryId);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public Long getCategoryId() { return categoryId; }
    public boolean isActive() { return active; }

    public void deactivate() {
        this.active = false;
    }

    public void update(
            String name,
            String slug,
            String description,
            BigDecimal basePrice,
            Long categoryId
    ) {
        this.name = normalizeText(name, "name", 255);
        this.slug = normalizeSlug(slug);
        this.description = normalizeNullable(description, 2_000);
        this.basePrice = normalizePrice(basePrice);
        this.categoryId = requirePositive(categoryId, "categoryId");
    }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }

    public static String normalizeSlug(String value) {
        String normalized = normalizeText(value, "slug", 255).toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("slug must contain lowercase letters, digits, and hyphens");
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

    private static String normalizeNullable(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.strip();
        if (normalized.length() > max) throw new IllegalArgumentException("description must not exceed " + max + " characters");
        return normalized;
    }

    private static BigDecimal normalizePrice(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.scale() < 0 || value.scale() > 2
                || value.precision() - value.scale() > 10 || value.precision() > 12) {
            throw new IllegalArgumentException("basePrice must be a non-negative amount with at most two decimals");
        }
        return value;
    }

    private static Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) throw new IllegalArgumentException(field + " must be positive");
        return value;
    }
}