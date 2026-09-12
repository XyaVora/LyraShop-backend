package com.lyrashop.promotion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.promotion.entity.Promotion;
import com.lyrashop.promotion.entity.PromotionProduct;
import com.lyrashop.promotion.repository.PromotionProductRepository;
import com.lyrashop.promotion.repository.PromotionRepository;

class PromotionServiceTests {
    @Test void returnsOnlyActiveProductsFromTheCurrentPromotion() {
        PromotionRepository promotions = mock(PromotionRepository.class);
        PromotionProductRepository items = mock(PromotionProductRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        Instant now = Instant.parse("2026-09-12T00:00:00Z");
        Promotion promotion = mock(Promotion.class);
        PromotionProduct activeItem = mock(PromotionProduct.class);
        PromotionProduct inactiveItem = mock(PromotionProduct.class);
        UUID promotionId = UUID.randomUUID();
        UUID activeProductId = UUID.randomUUID();
        UUID inactiveProductId = UUID.randomUUID();

        when(promotion.getId()).thenReturn(promotionId);
        when(promotions.findFirstByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(now, now))
                .thenReturn(Optional.of(promotion));
        when(items.findAllByPromotionIdOrderBySortOrderAscIdAsc(promotionId))
                .thenReturn(List.of(activeItem, inactiveItem));
        when(activeItem.getProductId()).thenReturn(activeProductId);
        when(activeItem.getSalePrice()).thenReturn(new BigDecimal("800.00"));
        when(activeItem.getOriginalPrice()).thenReturn(new BigDecimal("1000.00"));
        when(activeItem.getDiscountPercent()).thenReturn(20);
        when(inactiveItem.getProductId()).thenReturn(inactiveProductId);
        when(products.existsByIdAndActiveTrue(activeProductId)).thenReturn(true);
        when(products.existsByIdAndActiveTrue(inactiveProductId)).thenReturn(false);

        var service = new PromotionService(promotions, items, products, Clock.fixed(now, ZoneOffset.UTC));
        var result = service.active().orElseThrow();

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().productId()).isEqualTo(activeProductId);
    }
}
