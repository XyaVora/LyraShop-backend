package com.lyrashop.catalog.variant.entity;

import static org.hibernate.type.SqlTypes.BINARY;

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

@Entity @Table(name = "inventory_adjustments")
public class InventoryAdjustment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @JdbcTypeCode(BINARY) @Column(name="product_id",nullable=false,length=16) private UUID productId;
    @JdbcTypeCode(BINARY) @Column(name="variant_id",nullable=false,length=16) private UUID variantId;
    @JdbcTypeCode(BINARY) @Column(name="admin_id",length=16) private UUID adminId;
    @Column(name="movement_type",nullable=false,length=30) private String movementType;
    @JdbcTypeCode(BINARY) @Column(name="order_id",length=16) private UUID orderId;
    @Column(name="stock_before",nullable=false) private int stockBefore;
    @Column(name="stock_after",nullable=false) private int stockAfter;
    @Column(nullable=false,length=255) private String reason;
    @CreationTimestamp(source=SourceType.DB) @Column(name="created_at",nullable=false) private Instant createdAt;
    protected InventoryAdjustment() {}
    public InventoryAdjustment(UUID productId,UUID variantId,UUID adminId,int before,int after,String reason){
        this(productId,variantId,adminId,"MANUAL",null,before,after,reason);
    }
    public static InventoryAdjustment system(UUID productId,UUID variantId,UUID orderId,String movementType,int before,int after,String reason){
        return new InventoryAdjustment(productId,variantId,null,movementType,orderId,before,after,reason);
    }
    private InventoryAdjustment(UUID productId,UUID variantId,UUID adminId,String movementType,UUID orderId,int before,int after,String reason){this.productId=productId;this.variantId=variantId;this.adminId=adminId;this.movementType=movementType;this.orderId=orderId;this.stockBefore=before;this.stockAfter=after;this.reason=reason.strip();}
    public Long getId(){return id;} public UUID getProductId(){return productId;} public UUID getVariantId(){return variantId;} public UUID getAdminId(){return adminId;} public String getMovementType(){return movementType;} public UUID getOrderId(){return orderId;} public int getStockBefore(){return stockBefore;} public int getStockAfter(){return stockAfter;} public String getReason(){return reason;} public Instant getCreatedAt(){return createdAt;}
}
