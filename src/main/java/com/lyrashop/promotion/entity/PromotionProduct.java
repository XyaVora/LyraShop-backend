package com.lyrashop.promotion.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;
import static org.hibernate.type.SqlTypes.DECIMAL;

@Entity
@Table(name = "promotion_products")
public class PromotionProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @JdbcTypeCode(BINARY) @Column(name = "promotion_id", nullable = false, length = 16) private UUID promotionId;
    @JdbcTypeCode(BINARY) @Column(name = "product_id", nullable = false, length = 16) private UUID productId;
    @JdbcTypeCode(DECIMAL) @Column(name = "sale_price", nullable = false, precision = 12, scale = 2) private BigDecimal salePrice;
    @JdbcTypeCode(DECIMAL) @Column(name = "original_price", nullable = false, precision = 12, scale = 2) private BigDecimal originalPrice;
    @Column(name = "discount_percent", nullable = false) private int discountPercent;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    protected PromotionProduct() {}
    public Long getId() { return id; }
    public UUID getPromotionId() { return promotionId; }
    public UUID getProductId() { return productId; }
    public BigDecimal getSalePrice() { return salePrice; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public int getDiscountPercent() { return discountPercent; }
    public int getSortOrder() { return sortOrder; }
}
