package com.lyrashop.catalog.product.service;

import java.util.List;

import com.lyrashop.catalog.variant.service.ProductVariantResult;

import com.lyrashop.catalog.product.entity.ProductImage;

public record ProductDetailResult(
        ProductResult product,
        List<ProductVariantResult> variants,
        List<ProductImage> images
) {

    public ProductDetailResult {
        variants = List.copyOf(variants);
        images = List.copyOf(images);
    }
}
