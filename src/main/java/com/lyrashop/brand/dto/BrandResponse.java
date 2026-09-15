package com.lyrashop.brand.dto;

public record BrandResponse(
        String name, String tagline, String story, int founded,
        String hotline, String email, String address
) {}
