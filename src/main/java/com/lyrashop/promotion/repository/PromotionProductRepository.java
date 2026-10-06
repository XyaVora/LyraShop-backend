package com.lyrashop.promotion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;

import com.lyrashop.promotion.entity.PromotionProduct;
import com.lyrashop.promotion.entity.PromotionProductId;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, PromotionProductId> {
    List<PromotionProduct> findAllByPromotionIdOrderByProductIdAsc(UUID promotionId);
    void deleteAllByPromotionId(UUID promotionId);
    @Query("""
            select distinct item.productId
            from PromotionProduct item, Promotion promotion
            where item.promotionId = promotion.id
              and promotion.active = true
              and promotion.startsAt < :endsAt
              and promotion.endsAt > :startsAt
              and item.productId in :productIds
              and (:excludeId is null or promotion.id <> :excludeId)
            """)
    List<UUID> findOverlappingProductIds(@Param("productIds") Collection<UUID> productIds,
            @Param("startsAt") Instant startsAt, @Param("endsAt") Instant endsAt,
            @Param("excludeId") UUID excludeId);
}
