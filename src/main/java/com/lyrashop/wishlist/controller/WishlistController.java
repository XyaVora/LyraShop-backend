package com.lyrashop.wishlist.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.wishlist.dto.AddWishlistItemRequest;
import com.lyrashop.wishlist.dto.WishlistItemResponse;
import com.lyrashop.wishlist.service.WishlistService;
import com.lyrashop.wishlist.dto.WishlistShareResponse;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/wishlist")
@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
public class WishlistController {
    private final WishlistService service;
    public WishlistController(WishlistService service) { this.service = service; }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<WishlistItemResponse> list(Authentication auth) { return service.list(userId(auth)); }

    @PostMapping(path = "/items", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WishlistItemResponse> add(Authentication auth, @Valid @RequestBody AddWishlistItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(userId(auth), request.productId()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> remove(Authentication auth, @PathVariable UUID productId) {
        service.remove(userId(auth), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication auth) {
        service.clear(userId(auth));
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/share", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WishlistShareResponse> share(Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.share(userId(auth)));
    }

    private static UUID userId(Authentication auth) { return UUID.fromString(auth.getName()); }
}
