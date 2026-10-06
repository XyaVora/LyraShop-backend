package com.lyrashop.user.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_support_notes")
public class CustomerSupportNote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "customer_id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "created_by", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID createdBy;

    @Column(name = "note", nullable = false, length = 2000)
    private String note;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    protected CustomerSupportNote() { }

    private CustomerSupportNote(UUID customerId, UUID createdBy, String note) {
        if (customerId == null || createdBy == null) throw new IllegalArgumentException("customerId and createdBy are required");
        if (note == null || note.isBlank()) throw new IllegalArgumentException("note must not be blank");
        String clean = note.strip();
        if (clean.length() > 2000) throw new IllegalArgumentException("note must not exceed 2000 characters");
        this.customerId = customerId;
        this.createdBy = createdBy;
        this.note = clean;
    }

    public static CustomerSupportNote create(UUID customerId, UUID createdBy, String note) {
        return new CustomerSupportNote(customerId, createdBy, note);
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getCreatedBy() { return createdBy; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}
