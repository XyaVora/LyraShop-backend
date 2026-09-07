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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductImageResponse> upload(
            @PathVariable String productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(defaultValue = "false") boolean primary,
            @RequestParam(defaultValue = "0") int sortOrder
    ) {
        UUID productUuid;
        try {
            productUuid = UUID.fromString(productId);
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductImageResponse.from(
                        productImageService.createFromFile(productUuid, file, variantId, primary, sortOrder)
                ));
    }
}
