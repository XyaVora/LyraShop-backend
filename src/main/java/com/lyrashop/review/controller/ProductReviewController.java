package com.lyrashop.review.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.catalog.product.service.ProductNotFoundException;
import com.lyrashop.review.dto.CreateReviewRequest;
import com.lyrashop.review.dto.ReviewResponse;
import com.lyrashop.review.service.ReviewService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ProductReviewController {

    private final ReviewService reviewService;

    public ProductReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReviewResponse> list(@PathVariable String productId) {
        return reviewService.listForProduct(productUuid(productId));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReviewResponse> create(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                reviewService.create(UUID.fromString(authentication.getName()), productUuid(productId), request)
        );
    }

    private static UUID productUuid(String productId) {
        try {
            return UUID.fromString(productId);
        } catch (IllegalArgumentException exception) {
            throw new ProductNotFoundException();
        }
    }
}
