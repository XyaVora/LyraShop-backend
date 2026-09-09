package com.lyrashop.cart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.cart.dto.AddCartItemRequest;
import com.lyrashop.cart.dto.CartItemResponse;
import com.lyrashop.cart.dto.CartResponse;
import com.lyrashop.cart.dto.UpdateCartItemRequest;
import com.lyrashop.cart.entity.Cart;
import com.lyrashop.cart.entity.CartItem;
import com.lyrashop.cart.repository.CartItemRepository;
import com.lyrashop.cart.repository.CartRepository;
import com.lyrashop.catalog.category.repository.CategoryRepository;
import com.lyrashop.catalog.product.entity.Product;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.entity.ProductVariant;
import com.lyrashop.catalog.variant.repository.ProductVariantRepository;
import com.lyrashop.catalog.variant.service.VariantNotFoundException;

@Service
public class CartService {

    private final CartRepository carts;
    private final CartItemRepository items;
    private final ProductVariantRepository variants;
    private final ProductRepository products;
    private final CategoryRepository categories;

    public CartService(
            CartRepository carts,
            CartItemRepository items,
            ProductVariantRepository variants,
            ProductRepository products,
            CategoryRepository categories
    ) {
        this.carts = carts;
        this.items = items;
        this.variants = variants;
        this.products = products;
        this.categories = categories;
    }

    @Transactional
    public CartResponse get(UUID userId) {
        return toResponse(getOrCreate(userId));
    }

    @Transactional
    public CartResponse addItem(UUID userId, AddCartItemRequest request) {
        Cart cart = getOrCreate(userId);
        ProductVariant variant = requirePurchasable(request.variantId());
        CartItem existing = items.findByCartIdAndVariantId(cart.getId(), variant.getId()).orElse(null);
        int quantity = existing == null ? request.quantity() : existing.getQuantity() + request.quantity();
        if (quantity > 99 || quantity > variant.getStock()) {
            throw new InsufficientStockException();
        }
        if (existing == null) {
            try {
                items.saveAndFlush(CartItem.create(cart.getId(), variant.getId(), request.quantity()));
            } catch (DataIntegrityViolationException exception) {
                existing = items.findByCartIdAndVariantId(cart.getId(), variant.getId())
                        .orElseThrow(() -> exception);
                quantity = existing.getQuantity() + request.quantity();
                if (quantity > 99 || quantity > variant.getStock()) {
                    throw new InsufficientStockException();
                }
                existing.setQuantity(quantity);
                items.saveAndFlush(existing);
            }
        } else {
            existing.setQuantity(quantity);
            items.saveAndFlush(existing);
        }
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(UUID userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreate(userId);
        CartItem item = items.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(CartItemNotFoundException::new);
        ProductVariant variant = requirePurchasable(item.getVariantId());
        if (request.quantity() > variant.getStock()) {
            throw new InsufficientStockException();
        }
        item.setQuantity(request.quantity());
        items.saveAndFlush(item);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, Long itemId) {
        Cart cart = getOrCreate(userId);
        CartItem item = items.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(CartItemNotFoundException::new);
        items.delete(item);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse clear(UUID userId) {
        Cart cart = getOrCreate(userId);
        items.deleteAllByCartId(cart.getId());
        return toResponse(cart);
    }

    private Cart getOrCreate(UUID userId) {
        return carts.findByUserId(userId).orElseGet(() -> {
            try {
                return carts.saveAndFlush(Cart.create(userId));
            } catch (DataIntegrityViolationException exception) {
                return carts.findByUserId(userId).orElseThrow(() -> exception);
            }
        });
    }

    private ProductVariant requirePurchasable(UUID variantId) {
        ProductVariant variant = variants.findById(variantId).orElseThrow(VariantNotFoundException::new);
        if (!variant.isActive()) {
            throw new VariantNotFoundException();
        }
        Product product = products.findById(variant.getProductId()).orElseThrow(VariantNotFoundException::new);
        if (!product.isActive() || !categories.existsByIdAndActiveTrue(product.getCategoryId())) {
            throw new VariantNotFoundException();
        }
        return variant;
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItem> cartItems = items.findAllByCartIdOrderByIdAsc(cart.getId());
        List<CartItemResponse> responses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (CartItem item : cartItems) {
            ProductVariant variant = variants.findById(item.getVariantId()).orElseThrow(VariantNotFoundException::new);
            Product product = products.findById(variant.getProductId()).orElse(null);
            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            responses.add(new CartItemResponse(
                    item.getId(),
                    variant.getId(),
                    variant.getProductId(),
                    product == null ? variant.getSku() : product.getName(),
                    variant.getSku(),
                    variant.getSize(),
                    variant.getColor(),
                    variant.getPrice(),
                    item.getQuantity(),
                    subtotal
            ));
        }
        return new CartResponse(cart.getId(), responses, total);
    }
}
