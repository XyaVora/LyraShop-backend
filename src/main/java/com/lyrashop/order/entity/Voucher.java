package com.lyrashop.order.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.BINARY)
    @Column(nullable = false, updatable = false, length = 16)
    private UUID id;
    @Column(nullable = false, updatable = false, length = 30) private String code;
    @Column(nullable = false) private String label;
    @Column(name = "voucher_type", nullable = false, length = 20) private String type;
    @Column(name = "discount_type", nullable = false, length = 20) private String discountType;
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2) private BigDecimal discountValue;
    @Column(name = "max_discount_amount", precision = 12, scale = 2) private BigDecimal maxDiscountAmount;
    @Column(name = "minimum_order_amount", nullable = false, precision = 12, scale = 2) private BigDecimal minimumOrderAmount;
    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "ends_at", nullable = false) private Instant endsAt;
    @Column(name = "total_usage_limit") private Integer totalUsageLimit;
    @Column(name = "per_user_limit", nullable = false) private int perUserLimit;
    @Column(name = "is_active", nullable = false) private boolean active;

    protected Voucher() {}

    public static Voucher create(String code,String label,String type,String discountType,BigDecimal discountValue,
            BigDecimal maxDiscountAmount,BigDecimal minimumOrderAmount,Instant startsAt,Instant endsAt,
            Integer totalUsageLimit,int perUserLimit,boolean active){Voucher v=new Voucher();v.code=code;v.update(label,type,discountType,discountValue,maxDiscountAmount,minimumOrderAmount,startsAt,endsAt,totalUsageLimit,perUserLimit,active);return v;}
    public void update(String label,String type,String discountType,BigDecimal discountValue,BigDecimal maxDiscountAmount,
            BigDecimal minimumOrderAmount,Instant startsAt,Instant endsAt,Integer totalUsageLimit,int perUserLimit,boolean active){this.label=label;this.type=type;this.discountType=discountType;this.discountValue=discountValue;this.maxDiscountAmount=maxDiscountAmount;this.minimumOrderAmount=minimumOrderAmount;this.startsAt=startsAt;this.endsAt=endsAt;this.totalUsageLimit=totalUsageLimit;this.perUserLimit=perUserLimit;this.active=active;}
    public void deactivate(){active=false;}

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getType() { return type; }
    public String getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public Integer getTotalUsageLimit() { return totalUsageLimit; }
    public int getPerUserLimit() { return perUserLimit; }
    public boolean isActive() { return active; }
}
