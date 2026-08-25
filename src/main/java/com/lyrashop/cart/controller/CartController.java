package com.lyrashop.cart.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.cart.dto.AddCartItemRequest;
import com.lyrashop.cart.dto.CartResponse;
import com.lyrashop.cart.dto.UpdateCartItemRequest;
import com.lyrashop.cart.service.CartItemNotFoundException;
import com.lyrashop.cart.service.CartService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponse get(Authentication authentication) {
        return cartService.get(userId(authentication));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping(path = "/items", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartResponse> add(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(userId(authentication), request));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PutMapping(path = "/items/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponse update(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItem(userId(authentication), itemId, request);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @DeleteMapping(path = "/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponse remove(Authentication authentication, @PathVariable Long itemId) {
        return cartService.removeItem(userId(authentication), itemId);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication authentication) {
        cartService.clear(userId(authentication));
        return ResponseEntity.noContent().build();
    }

    private static UUID userId(Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (RuntimeException exception) {
            throw new CartItemNotFoundException();
        }
    }
}
