package com.lyrashop.promotion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.promotion.entity.PromotionProduct;
import com.lyrashop.promotion.entity.PromotionProductId;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, PromotionProductId> {
    List<PromotionProduct> findAllByPromotionIdOrderByProductIdAsc(UUID promotionId);
    void deleteAllByPromotionId(UUID promotionId);
}
