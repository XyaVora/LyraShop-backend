package com.lyrashop.order.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "voucher_redemptions")
public class VoucherRedemption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "voucher_id", nullable = false, updatable = false, length = 16) private UUID voucherId;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "user_id", nullable = false, updatable = false, length = 16) private UUID userId;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "order_id", nullable = false, updatable = false, length = 16) private UUID orderId;

    protected VoucherRedemption() {}
    private VoucherRedemption(UUID voucherId, UUID userId, UUID orderId) {
        this.voucherId = voucherId; this.userId = userId; this.orderId = orderId;
    }
    public static VoucherRedemption create(UUID voucherId, UUID userId, UUID orderId) {
        return new VoucherRedemption(voucherId, userId, orderId);
    }
}
