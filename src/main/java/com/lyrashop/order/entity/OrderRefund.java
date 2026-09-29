package com.lyrashop.order.entity;

import static org.hibernate.type.SqlTypes.BINARY;
import static org.hibernate.type.SqlTypes.DECIMAL;

import java.math.BigDecimal;
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

@Entity
@Table(name = "order_refunds")
public class OrderRefund {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(BINARY)
    @Column(length = 16) private UUID id;
    @JdbcTypeCode(BINARY) @Column(name = "order_id", nullable = false, length = 16) private UUID orderId;
    @JdbcTypeCode(BINARY) @Column(name = "return_request_id", length = 16) private UUID returnRequestId;
    @JdbcTypeCode(BINARY) @Column(name = "processed_by", nullable = false, length = 16) private UUID processedBy;
    @JdbcTypeCode(DECIMAL) @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(nullable = false, unique = true, length = 100) private String reference;
    @Column(length = 500) private String note;
    @CreationTimestamp(source = SourceType.DB) @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected OrderRefund() {}

    public OrderRefund(UUID orderId, UUID returnRequestId, UUID processedBy, BigDecimal amount,
            String reference, String note) {
        this.orderId = orderId;
        this.returnRequestId = returnRequestId;
        this.processedBy = processedBy;
        this.amount = amount;
        this.reference = reference.strip();
        this.note = note == null || note.isBlank() ? null : note.strip();
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getReturnRequestId() { return returnRequestId; }
    public UUID getProcessedBy() { return processedBy; }
    public BigDecimal getAmount() { return amount; }
    public String getReference() { return reference; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}
