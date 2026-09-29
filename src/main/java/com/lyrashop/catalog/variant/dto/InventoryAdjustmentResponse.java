package com.lyrashop.catalog.variant.dto;
import java.time.Instant;import java.util.UUID;import com.lyrashop.catalog.variant.entity.InventoryAdjustment;
public record InventoryAdjustmentResponse(Long id,UUID variantId,UUID adminId,int stockBefore,int stockAfter,String reason,Instant createdAt){public static InventoryAdjustmentResponse from(InventoryAdjustment a){return new InventoryAdjustmentResponse(a.getId(),a.getVariantId(),a.getAdminId(),a.getStockBefore(),a.getStockAfter(),a.getReason(),a.getCreatedAt());}}
