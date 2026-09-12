package com.lyrashop.promotion.service;

import java.time.Clock;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.promotion.dto.PromotionProductResponse;
import com.lyrashop.promotion.dto.PromotionResponse;
import com.lyrashop.promotion.repository.PromotionProductRepository;
import com.lyrashop.promotion.repository.PromotionRepository;

@Service
public class PromotionService {
    private final PromotionRepository promotions;
    private final PromotionProductRepository items;
    private final ProductRepository products;
    private final Clock clock;

    @Autowired
    public PromotionService(PromotionRepository promotions, PromotionProductRepository items, ProductRepository products) {
        this(promotions, items, products, Clock.systemUTC());
    }

    PromotionService(PromotionRepository promotions, PromotionProductRepository items, ProductRepository products, Clock clock) {
        this.promotions = promotions;
        this.items = items;
        this.products = products;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> activePrices() {
        Instant now = clock.instant();
        return promotions.findFirstByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(now, now)
                .map(promotion -> items.findAllByPromotionIdOrderBySortOrderAscIdAsc(promotion.getId()).stream()
                        .filter(item -> products.existsByIdAndActiveTrue(item.getProductId()))
                        .collect(Collectors.toUnmodifiableMap(
                                item -> item.getProductId(),
                                item -> item.getSalePrice()
                        )))
                .orElseGet(Map::of);
    }

    @Transactional(readOnly = true)
    public Optional<PromotionResponse> active() {
        Instant now = clock.instant();
        return promotions.findFirstByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(now, now)
                .map(promotion -> PromotionResponse.from(promotion,
                        items.findAllByPromotionIdOrderBySortOrderAscIdAsc(promotion.getId()).stream()
                                .filter(item -> products.existsByIdAndActiveTrue(item.getProductId()))
                                .map(PromotionProductResponse::from).toList()));
    }
}
