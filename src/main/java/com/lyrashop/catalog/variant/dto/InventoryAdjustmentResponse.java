package com.lyrashop.catalog.variant.dto;
import java.time.Instant;import java.util.UUID;import com.lyrashop.catalog.variant.entity.InventoryAdjustment;
public record InventoryAdjustmentResponse(Long id,UUID variantId,UUID adminId,String movementType,UUID orderId,int stockBefore,int stockAfter,String reason,Instant createdAt){public static InventoryAdjustmentResponse from(InventoryAdjustment a){return new InventoryAdjustmentResponse(a.getId(),a.getVariantId(),a.getAdminId(),a.getMovementType(),a.getOrderId(),a.getStockBefore(),a.getStockAfter(),a.getReason(),a.getCreatedAt());}}
