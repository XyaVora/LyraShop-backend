package com.lyrashop.catalog.product.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.lyrashop.catalog.product.service.ProductResult;

public record ProductPageResponse(
        List<ProductResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public ProductPageResponse {
        content = List.copyOf(content);
    }

    public static ProductPageResponse from(Page<ProductResult> products) {
        return new ProductPageResponse(
                products.getContent().stream().map(ProductResponse::from).toList(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages()
        );
    }
}