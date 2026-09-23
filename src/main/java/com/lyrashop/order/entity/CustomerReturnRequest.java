package com.lyrashop.order.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_return_requests")
public class CustomerReturnRequest {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16) private UUID id;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "order_id", length = 16, nullable = false) private UUID orderId;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "user_id", length = 16, nullable = false) private UUID userId;
    @Column(nullable = false, length = 30) private String status;
    @Column(nullable = false, length = 1000) private String reason;
    @CreationTimestamp(source = SourceType.DB) @Column(name = "created_at") private Instant createdAt;
    @UpdateTimestamp(source = SourceType.DB) @Column(name = "updated_at") private Instant updatedAt;

    protected CustomerReturnRequest() {}
    private CustomerReturnRequest(UUID orderId, UUID userId, String reason) {
        this.orderId = orderId; this.userId = userId; this.reason = reason; this.status = "REQUESTED";
    }
    public static CustomerReturnRequest create(UUID orderId, UUID userId, String reason) {
        return new CustomerReturnRequest(orderId, userId, reason);
    }
    public UUID getId(){return id;} public UUID getOrderId(){return orderId;} public UUID getUserId(){return userId;}
    public String getStatus(){return status;} public String getReason(){return reason;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
