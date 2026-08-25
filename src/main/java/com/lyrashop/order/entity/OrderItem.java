package com.lyrashop.order.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;
import static org.hibernate.type.SqlTypes.DECIMAL;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private ShopOrder order;

    @JdbcTypeCode(BINARY)
    @Column(name = "variant_id", nullable = false, updatable = false, length = 16)
    private UUID variantId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "size", nullable = false, length = 20)
    private String size;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    protected OrderItem() {
    }

    private OrderItem(
            UUID variantId,
            String productName,
            String sku,
            String size,
            String color,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        this.variantId = variantId;
        this.productName = productName;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public static OrderItem snapshot(
            UUID variantId,
            String productName,
            String sku,
            String size,
            String color,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        return new OrderItem(variantId, productName, sku, size, color, quantity, unitPrice, subtotal);
    }

    void setOrder(ShopOrder order) {
        this.order = order;
    }

    public Long getId() { return id; }
    public UUID getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getSku() { return sku; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
}
