package com.lyrashop.catalog.product.service;

import java.util.List;

import com.lyrashop.catalog.variant.service.ProductVariantResult;

public record ProductDetailResult(
        ProductResult product,
        List<ProductVariantResult> variants
) {

    public ProductDetailResult {
        variants = List.copyOf(variants);
    }
}
