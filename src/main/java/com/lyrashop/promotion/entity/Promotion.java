package com.lyrashop.promotion.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id @JdbcTypeCode(BINARY) @Column(name = "id", nullable = false, length = 16) private UUID id;
    @Column(name = "title", nullable = false, length = 255) private String title;
    @Column(name = "subtitle", length = 255) private String subtitle;
    @Column(name = "description", length = 1000) private String description;
    @Column(name = "badge", length = 100) private String badge;
    @Column(name = "starts_at", nullable = false, columnDefinition = "datetime(6)") private Instant startsAt;
    @Column(name = "ends_at", nullable = false, columnDefinition = "datetime(6)") private Instant endsAt;
    @Column(name = "is_active", nullable = false, columnDefinition = "boolean") private boolean active;
    protected Promotion() {}
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getDescription() { return description; }
    public String getBadge() { return badge; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public boolean isActive() { return active; }
}
