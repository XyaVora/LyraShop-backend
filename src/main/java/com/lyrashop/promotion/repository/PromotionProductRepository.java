package com.lyrashop.promotion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.promotion.entity.PromotionProduct;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {
    List<PromotionProduct> findAllByPromotionIdOrderBySortOrderAscIdAsc(UUID promotionId);
}
