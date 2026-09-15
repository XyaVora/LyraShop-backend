package com.lyrashop.wishlist.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lyrashop.wishlist.dto.WishlistItemResponse;
import com.lyrashop.wishlist.service.WishlistService;

@RestController @RequestMapping("/api/v1/wishlist/shared")
public class PublicWishlistController {
    private final WishlistService service;
    public PublicWishlistController(WishlistService service) { this.service = service; }
    @GetMapping(path = "/{shareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<WishlistItemResponse> get(@PathVariable UUID shareId) { return service.shared(shareId); }
}
