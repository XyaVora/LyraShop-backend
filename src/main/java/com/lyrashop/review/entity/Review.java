package com.lyrashop.review.entity;

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
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(BINARY)
    @Column(name = "product_id", nullable = false, updatable = false, length = 16)
    private UUID productId;

    @JdbcTypeCode(BINARY)
    @Column(name = "user_id", nullable = false, updatable = false, length = 16)
    private UUID userId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "moderation_status", nullable = false, length = 20)
    private String moderationStatus = "PUBLISHED";

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;

    @JdbcTypeCode(BINARY)
    @Column(name = "moderated_by", length = 16)
    private UUID moderatedBy;

    @Column(name = "moderated_at")
    private Instant moderatedAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    protected Review() {
    }

    private Review(UUID productId, UUID userId, int rating, String comment) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("rating must be 1 to 5");
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment == null || comment.isBlank() ? null : comment.strip();
    }

    public static Review create(UUID productId, UUID userId, int rating, String comment) {
        return new Review(productId, userId, rating, comment);
    }

    public Long getId() { return id; }
    public UUID getProductId() { return productId; }
    public UUID getUserId() { return userId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getModerationStatus() { return moderationStatus; }
    public String getModerationNote() { return moderationNote; }
    public UUID getModeratedBy() { return moderatedBy; }
    public Instant getModeratedAt() { return moderatedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void moderate(String status, String note, UUID adminId) {
        if (!"PUBLISHED".equals(status) && !"HIDDEN".equals(status)) throw new IllegalArgumentException("invalid moderation status");
        this.moderationStatus = status;
        this.moderationNote = note == null || note.isBlank() ? null : note.strip();
        this.moderatedBy = adminId;
        this.moderatedAt = Instant.now();
    }
}
