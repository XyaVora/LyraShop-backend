package com.lyrashop.wishlist.dto;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.wishlist.entity.WishlistItem;

public record WishlistItemResponse(Long id, UUID productId, Instant createdAt) {
    public static WishlistItemResponse from(WishlistItem item) {
        return new WishlistItemResponse(item.getId(), item.getProductId(), item.getCreatedAt());
    }
}
