package com.lyrashop.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lyrashop.cart.entity.Cart;
import com.lyrashop.cart.repository.CartItemRepository;
import com.lyrashop.cart.repository.CartRepository;
import com.lyrashop.catalog.category.repository.CategoryRepository;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.repository.ProductVariantRepository;
import com.lyrashop.promotion.service.PromotionService;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTests {
    @Mock CartRepository carts;
    @Mock CartItemRepository items;
    @Mock ProductVariantRepository variants;
    @Mock ProductRepository products;
    @Mock CategoryRepository categories;
    @Mock PromotionService promotions;
    @Mock UserRepository users;

    private CartService service;

    @BeforeEach
    void setUp() {
        service = new CartService(carts, items, variants, products, categories, promotions, users);
    }

    @Test
    void locksTheUserBeforeCreatingAMissingCart() {
        UUID userId = UUID.randomUUID();
        Cart created = Cart.create(userId);
        when(users.findByIdForUpdate(userId)).thenReturn(Optional.of(org.mockito.Mockito.mock(User.class)));
        when(carts.findByUserId(userId)).thenReturn(Optional.empty());
        when(carts.saveAndFlush(any(Cart.class))).thenReturn(created);
        when(items.findAllByCartIdOrderByIdAsc(null)).thenReturn(List.of());
        when(promotions.activePrices()).thenReturn(Map.of());

        assertThat(service.get(userId).items()).isEmpty();

        InOrder order = inOrder(users, carts);
        order.verify(users).findByIdForUpdate(userId);
        order.verify(carts).findByUserId(userId);
        order.verify(carts).saveAndFlush(any(Cart.class));
        verify(items).findAllByCartIdOrderByIdAsc(null);
    }
}
