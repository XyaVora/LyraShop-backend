package com.lyrashop.promotion.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(BINARY)
    @Column(name = "id", nullable = false, length = 16) private UUID id;
    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "description", columnDefinition = "text") private String description;
    @Column(name = "discount_percent", nullable = false) private int discountPercent;
    @Column(name = "start_at", nullable = false, columnDefinition = "datetime(6)") private Instant startsAt;
    @Column(name = "end_at", nullable = false, columnDefinition = "datetime(6)") private Instant endsAt;
    @Column(name = "is_active", nullable = false, columnDefinition = "boolean") private boolean active;
    protected Promotion() {}
    private Promotion(String name, String description, int discountPercent, Instant startsAt, Instant endsAt, boolean active) {
        update(name, description, discountPercent, startsAt, endsAt, active);
    }
    public static Promotion create(String name, String description, int discountPercent, Instant startsAt, Instant endsAt, boolean active) {
        return new Promotion(name, description, discountPercent, startsAt, endsAt, active);
    }
    public void update(String name, String description, int discountPercent, Instant startsAt, Instant endsAt, boolean active) {
        this.name = name.strip(); this.description = description == null || description.isBlank() ? null : description.strip();
        this.discountPercent = discountPercent; this.startsAt = startsAt; this.endsAt = endsAt; this.active = active;
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDiscountPercent() { return discountPercent; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public boolean isActive() { return active; }
}
