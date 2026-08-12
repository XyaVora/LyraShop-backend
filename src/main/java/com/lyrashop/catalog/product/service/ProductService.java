package com.lyrashop.catalog.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.catalog.category.entity.Category;
import com.lyrashop.catalog.category.repository.CategoryRepository;
import com.lyrashop.catalog.product.entity.Product;
import com.lyrashop.catalog.product.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResult> list(ProductQuery query) {
        PageRequest pageable = PageRequest.of(query.page(), query.size(), query.sort().toSort());
        var specification = ProductSpecifications.active()
                .and(ProductSpecifications.categoryActive());
        if (query.keyword() != null) {
            specification = specification.and(ProductSpecifications.keyword(query.keyword()));
        }
        if (query.minPrice() != null) {
            specification = specification.and(ProductSpecifications.minPrice(query.minPrice()));
        }
        if (query.maxPrice() != null) {
            specification = specification.and(ProductSpecifications.maxPrice(query.maxPrice()));
        }
        if (query.categorySlug() != null) {
            var categoryId = categoryRepository.findBySlugAndActiveTrue(query.categorySlug())
                    .map(Category::getId)
                    .orElse(null);
            if (categoryId == null) {
                return Page.empty(pageable);
            }
            specification = specification.and(ProductSpecifications.categoryId(categoryId));
        }
        return productRepository.findAll(specification, pageable).map(ProductResult::from);
    }

    @Transactional(readOnly = true)
    public ProductResult getActive(UUID id) {
        var specification = ProductSpecifications.id(id)
                .and(ProductSpecifications.active())
                .and(ProductSpecifications.categoryActive());
        return productRepository.findOne(specification)
                .map(ProductResult::from)
                .orElseThrow(ProductNotFoundException::new);
    }
}