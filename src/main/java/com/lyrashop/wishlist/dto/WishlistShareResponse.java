package com.lyrashop.wishlist.dto;
import java.time.Instant;
import java.util.UUID;
public record WishlistShareResponse(UUID id, Instant expiresAt) {}
