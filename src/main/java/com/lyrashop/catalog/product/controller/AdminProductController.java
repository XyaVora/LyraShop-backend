package com.lyrashop.catalog.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.catalog.product.dto.AdminProductDetailResponse;
import com.lyrashop.catalog.product.dto.AdminProductResponse;
import com.lyrashop.catalog.product.dto.CreateProductRequest;
import com.lyrashop.catalog.product.dto.ProductResponse;
import com.lyrashop.catalog.product.dto.UpdateProductRequest;
import com.lyrashop.catalog.product.service.ProductNotFoundException;
import com.lyrashop.catalog.product.service.ProductService;
import com.lyrashop.common.dto.PageResponse;
import com.lyrashop.common.web.AdminPageable;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminProductResponse> list() {
        return productService.listForAdmin();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @GetMapping(path = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<AdminProductResponse> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        var result = productService.pageForAdmin(query, active, AdminPageable.of(
                page, size, sort, direction,
                java.util.Set.of("name", "slug", "basePrice", "categoryId", "active", "createdAt", "updatedAt"),
                "createdAt"));
        return PageResponse.from(result, result.getContent());
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminProductDetailResponse get(@PathVariable String id) {
        try {
            return productService.getForAdmin(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(productService.create(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
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

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @PatchMapping(path = "/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        try {
            productService.deactivate(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @PatchMapping(path = "/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable String id) {
        try {
            productService.activate(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
        return ResponseEntity.noContent().build();
    }
}
