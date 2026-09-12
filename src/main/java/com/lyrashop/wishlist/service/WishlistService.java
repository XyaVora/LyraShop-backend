package com.lyrashop.wishlist.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.product.service.ProductNotFoundException;
import com.lyrashop.wishlist.dto.WishlistItemResponse;
import com.lyrashop.wishlist.entity.WishlistItem;
import com.lyrashop.wishlist.repository.WishlistItemRepository;

@Service
public class WishlistService {
    private final WishlistItemRepository items;
    private final ProductRepository products;

    public WishlistService(WishlistItemRepository items, ProductRepository products) {
        this.items = items;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> list(UUID userId) {
        return items.findAllByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
                .map(WishlistItemResponse::from).toList();
    }

    @Transactional
    public WishlistItemResponse add(UUID userId, UUID productId) {
        if (!products.existsByIdAndActiveTrue(productId)) throw new ProductNotFoundException();
        var existing = items.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) return WishlistItemResponse.from(existing.get());
        try {
            return WishlistItemResponse.from(items.saveAndFlush(WishlistItem.create(userId, productId)));
        } catch (DataIntegrityViolationException exception) {
            return items.findByUserIdAndProductId(userId, productId)
                    .map(WishlistItemResponse::from).orElseThrow(() -> exception);
        }
    }

    @Transactional
    public void remove(UUID userId, UUID productId) { items.deleteByUserIdAndProductId(userId, productId); }

    @Transactional
    public void clear(UUID userId) { items.deleteAllByUserId(userId); }
}
