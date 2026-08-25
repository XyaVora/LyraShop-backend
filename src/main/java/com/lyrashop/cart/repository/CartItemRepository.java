package com.lyrashop.cart.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCartIdOrderByIdAsc(UUID cartId);

    Optional<CartItem> findByCartIdAndVariantId(UUID cartId, UUID variantId);

    Optional<CartItem> findByIdAndCartId(Long id, UUID cartId);

    void deleteAllByCartId(UUID cartId);
}
