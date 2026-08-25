package com.lyrashop.catalog.product.entity;

import java.net.URI;
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
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @JdbcTypeCode(BINARY)
    @Column(name = "product_id", nullable = false, updatable = false, length = 16)
    private UUID productId;

    @JdbcTypeCode(BINARY)
    @Column(name = "variant_id", length = 16)
    private UUID variantId;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "is_primary", nullable = false, columnDefinition = "boolean")
    private boolean primary;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private Instant updatedAt;

    protected ProductImage() {
    }

    private ProductImage(UUID productId, UUID variantId, String url, boolean primary, int sortOrder) {
        this.productId = requireId(productId, "productId");
        this.variantId = variantId;
        this.url = normalizeUrl(url);
        this.primary = primary;
        if (sortOrder < 0) {
            throw new IllegalArgumentException("sortOrder must be non-negative");
        }
        this.sortOrder = sortOrder;
    }

    public static ProductImage create(
            UUID productId,
            UUID variantId,
            String url,
            boolean primary,
            int sortOrder
    ) {
        return new ProductImage(productId, variantId, url, primary, sortOrder);
    }

    public void clearPrimary() {
        this.primary = false;
    }

    public Long getId() { return id; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public String getUrl() { return url; }
    public boolean isPrimary() { return primary; }
    public int getSortOrder() { return sortOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    private static UUID requireId(UUID value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    public static String normalizeUrl(String value) {
        if (value == null) throw new IllegalArgumentException("url is required");
        String normalized = value.strip();
        if (normalized.isEmpty() || normalized.length() > 2048) {
            throw new IllegalArgumentException("url must be 1 to 2048 characters");
        }
        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("url must be an https URI");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getHost().isBlank()
                || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("url must be an https URI");
        }
        return uri.toASCIIString();
    }
}
