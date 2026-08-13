package com.lyrashop.catalog.product.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.catalog.product.dto.CreateProductRequest;
import com.lyrashop.catalog.product.dto.ProductResponse;
import com.lyrashop.catalog.product.dto.UpdateProductRequest;
import com.lyrashop.catalog.product.service.ProductNotFoundException;
import com.lyrashop.catalog.product.service.ProductService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(productService.create(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProductResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        try {
            return ResponseEntity.ok(ProductResponse.from(
                    productService.update(UUID.fromString(id), request)
            ));
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        try {
            productService.deactivate(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
        return ResponseEntity.noContent().build();
    }
}
