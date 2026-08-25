package com.lyrashop.catalog.product.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.catalog.product.dto.CreateProductImageRequest;
import com.lyrashop.catalog.product.dto.ProductImageResponse;
import com.lyrashop.catalog.product.service.ProductImageService;
import com.lyrashop.catalog.product.service.ProductNotFoundException;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/products/{productId}/images")
public class AdminProductImageController {

    private final ProductImageService productImageService;

    public AdminProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductImageResponse> create(
            @PathVariable String productId,
            @Valid @RequestBody CreateProductImageRequest request
    ) {
        UUID productUuid;
        try {
            productUuid = UUID.fromString(productId);
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductImageResponse.from(productImageService.create(productUuid, request)));
    }
}
