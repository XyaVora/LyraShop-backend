package com.lyrashop.promotion.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;
import static org.hibernate.type.SqlTypes.DECIMAL;

@Entity
@Table(name = "promotion_products")
@IdClass(PromotionProductId.class)
public class PromotionProduct {
    @Id @JdbcTypeCode(BINARY) @Column(name = "promotion_id", nullable = false, length = 16) private UUID promotionId;
    @Id @JdbcTypeCode(BINARY) @Column(name = "product_id", nullable = false, length = 16) private UUID productId;
    @JdbcTypeCode(DECIMAL) @Column(name = "sale_price", nullable = false, precision = 12, scale = 2) private BigDecimal salePrice;
    @JdbcTypeCode(DECIMAL) @Column(name = "original_price", nullable = false, precision = 12, scale = 2) private BigDecimal originalPrice;
    @Column(name = "discount_percent", nullable = false) private int discountPercent;
    protected PromotionProduct() {}
    public UUID getPromotionId() { return promotionId; }
    public UUID getProductId() { return productId; }
    public BigDecimal getSalePrice() { return salePrice; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public int getDiscountPercent() { return discountPercent; }
}
