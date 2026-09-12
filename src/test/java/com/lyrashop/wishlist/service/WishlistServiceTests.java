package com.lyrashop.wishlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.wishlist.entity.WishlistItem;
import com.lyrashop.wishlist.repository.WishlistItemRepository;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTests {
    @Mock WishlistItemRepository items;
    @Mock ProductRepository products;
    private WishlistService service;

    @BeforeEach void setUp() { service = new WishlistService(items, products); }

    @Test void addsAProductForTheAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(products.existsByIdAndActiveTrue(productId)).thenReturn(true);
        when(items.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        when(items.saveAndFlush(any(WishlistItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.add(userId, productId).productId()).isEqualTo(productId);
        verify(items).saveAndFlush(any(WishlistItem.class));
    }

    @Test void addingAnExistingProductIsIdempotent() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        WishlistItem existing = WishlistItem.create(userId, productId);
        when(products.existsByIdAndActiveTrue(productId)).thenReturn(true);
        when(items.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(existing));

        assertThat(service.add(userId, productId).productId()).isEqualTo(productId);
        verify(items, never()).saveAndFlush(any());
    }
}
