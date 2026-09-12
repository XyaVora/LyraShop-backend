package com.lyrashop.wishlist.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddWishlistItemRequest(@NotNull UUID productId) {}
