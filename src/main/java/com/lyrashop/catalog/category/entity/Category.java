package com.lyrashop.catalog.category.entity;

import java.time.Instant;
import java.util.Locale;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean")
    private boolean active;

    @CreationTimestamp(source = SourceType.DB)
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "datetime(6)"
    )
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private Instant updatedAt;

    protected Category() {
    }

    private Category(String name, String slug, String description, Long parentId) {
        this.name = normalizeText(name, "name", 100);
        this.slug = normalizeSlug(slug);
        this.description = normalizeNullable(description, 2_000);
        this.parentId = validateParentId(parentId);
        this.active = true;
    }

    public static Category create(
            String name,
            String slug,
            String description,
            Long parentId
    ) {
        return new Category(name, slug, description, parentId);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Long getParentId() {
        return parentId;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String name, String slug, String description, Long parentId) {
        this.name = normalizeText(name, "name", 100);
        this.slug = normalizeSlug(slug);
        this.description = normalizeNullable(description, 2_000);
        this.parentId = validateParentId(parentId);
    }

    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static String normalizeSlug(String value) {
        String normalized = normalizeText(value, "slug", 100).toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("slug must contain lowercase letters, digits, and hyphens");
        }
        return normalized;
    }

    private static String normalizeText(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        String normalized = value.strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static String normalizeNullable(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("description must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static Long validateParentId(Long parentId) {
        if (parentId != null && parentId <= 0) {
            throw new IllegalArgumentException("parentId must be positive");
        }
        return parentId;
    }
}
