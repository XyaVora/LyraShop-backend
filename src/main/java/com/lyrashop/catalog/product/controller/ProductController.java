package com.lyrashop.catalog.product.controller;

import java.util.UUID;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.catalog.product.dto.ProductDetailResponse;
import com.lyrashop.catalog.product.dto.ProductPageResponse;
import com.lyrashop.catalog.product.dto.ProductResponse;
import com.lyrashop.catalog.product.service.ProductNotFoundException;
import com.lyrashop.catalog.product.service.ProductQuery;
import com.lyrashop.catalog.product.service.ProductService;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductPageResponse list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String variantSize,
            @RequestParam(required = false) String clothingSize,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String pageSize
    ) {
        ProductQuery query = ProductQuery.from(
                keyword, category, variantSize, color, minPrice, maxPrice, sort, page, size,
                clothingSize, pageSize
        );
        return ProductPageResponse.from(productService.list(query));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductDetailResponse get(@PathVariable String id) {
        UUID productId;
        try {
            productId = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
        return ProductDetailResponse.from(productService.getActiveDetail(productId));
    }

    @GetMapping(path = "/featured", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductResponse> featured(@RequestParam(defaultValue = "8") int size) {
        return productService.featured(size).stream().map(ProductResponse::from).toList();
    }

    @GetMapping(path = "/{id}/related", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductResponse> related(@PathVariable String id,
            @RequestParam(defaultValue = "4") int size) {
        try {
            return productService.related(UUID.fromString(id), size).stream().map(ProductResponse::from).toList();
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
    }
}
