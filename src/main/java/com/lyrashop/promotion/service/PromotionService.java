package com.lyrashop.promotion.service;

import java.time.Clock;
import java.time.Instant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
import com.lyrashop.promotion.dto.PromotionRequest;
import com.lyrashop.promotion.entity.Promotion;
import com.lyrashop.promotion.entity.PromotionProduct;
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
                .map(promotion -> items.findAllByPromotionIdOrderByProductIdAsc(promotion.getId()).stream()
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
                        items.findAllByPromotionIdOrderByProductIdAsc(promotion.getId()).stream()
                                .filter(item -> products.existsByIdAndActiveTrue(item.getProductId()))
                                .map(PromotionProductResponse::from).toList()));
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> list() {
        return promotions.findAll().stream().map(this::response).toList();
    }

    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        validate(request);
        Promotion promotion = promotions.saveAndFlush(Promotion.create(request.name(), request.description(),
                request.discountPercent(), request.startsAt(), request.endsAt(), request.active()));
        replaceItems(promotion, request);
        return response(promotion);
    }

    @Transactional
    public PromotionResponse update(UUID id, PromotionRequest request) {
        validate(request);
        Promotion promotion = promotions.findById(id).orElseThrow(() -> new IllegalArgumentException("Promotion not found"));
        promotion.update(request.name(), request.description(), request.discountPercent(),
                request.startsAt(), request.endsAt(), request.active());
        promotions.saveAndFlush(promotion);
        items.deleteAllByPromotionId(id);
        items.flush();
        replaceItems(promotion, request);
        return response(promotion);
    }

    @Transactional
    public void delete(UUID id) {
        if (!promotions.existsById(id)) throw new IllegalArgumentException("Promotion not found");
        promotions.deleteById(id);
    }

    private void validate(PromotionRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) throw new IllegalArgumentException("Promotion end must be after start");
        if (request.productIds().stream().distinct().count() != request.productIds().size()) throw new IllegalArgumentException("Duplicate product");
    }

    private void replaceItems(Promotion promotion, PromotionRequest request) {
        BigDecimal factor = BigDecimal.valueOf(100L - request.discountPercent()).movePointLeft(2);
        var rows = request.productIds().stream().map(id -> {
            var product = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
            BigDecimal original = product.getBasePrice();
            return PromotionProduct.create(promotion.getId(), id,
                    original.multiply(factor).setScale(2, RoundingMode.HALF_UP), original,
                    request.discountPercent());
        }).toList();
        items.saveAll(rows);
        items.flush();
    }

    private PromotionResponse response(Promotion promotion) {
        return PromotionResponse.from(promotion, items.findAllByPromotionIdOrderByProductIdAsc(promotion.getId())
                .stream().filter(item -> products.existsByIdAndActiveTrue(item.getProductId()))
                .map(PromotionProductResponse::from).toList());
    }
}
