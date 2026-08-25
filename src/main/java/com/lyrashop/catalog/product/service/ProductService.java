package com.lyrashop.catalog.product.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.catalog.category.entity.Category;
import com.lyrashop.catalog.category.repository.CategoryRepository;
import com.lyrashop.catalog.product.dto.AdminProductResponse;
import com.lyrashop.catalog.product.dto.CreateProductRequest;
import com.lyrashop.catalog.product.dto.UpdateProductRequest;
import com.lyrashop.catalog.product.entity.Product;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.service.ProductVariantService;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantService productVariantService;
    private final ProductImageService productImageService;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductVariantService productVariantService,
            ProductImageService productImageService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantService = productVariantService;
        this.productImageService = productImageService;
    }

    @Transactional
    public ProductResult create(CreateProductRequest request) {
        if (!categoryRepository.existsByIdAndActiveTrue(request.categoryId())) {
            throw new ProductCategoryNotFoundException();
        }
        if (productRepository.existsBySlug(request.slug())) {
            throw new ProductSlugAlreadyExistsException();
        }

        Product product = Product.create(
                request.name(),
                request.slug(),
                request.description(),
                request.basePrice(),
                request.categoryId()
        );
        try {
            return ProductResult.from(productRepository.saveAndFlush(product));
        } catch (DataIntegrityViolationException exception) {
            if (isSlugUniqueViolation(exception)) {
                throw new ProductSlugAlreadyExistsException(exception);
            }
            throw exception;
        }
    }

    @Transactional
    public ProductResult update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        if (product.getVersion() != request.version()) {
            throw new ProductVersionConflictException();
        }
        if (!categoryRepository.existsByIdAndActiveTrue(request.categoryId())) {
            throw new ProductCategoryNotFoundException();
        }
        if (productRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ProductSlugAlreadyExistsException();
        }
        product.update(
                request.name(), request.slug(), request.description(),
                request.basePrice(), request.categoryId()
        );
        try {
            return ProductResult.from(productRepository.saveAndFlush(product));
        } catch (DataIntegrityViolationException exception) {
            if (isSlugUniqueViolation(exception)) {
                throw new ProductSlugAlreadyExistsException(exception);
            }
            throw exception;
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ProductVersionConflictException(exception);
        }
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        product.deactivate();
        productRepository.saveAndFlush(product);
    }

    @Transactional(readOnly = true)
    public List<AdminProductResponse> listForAdmin() {
        return productRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(AdminProductResponse::from)
                .toList();
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
        if (query.variantSize() != null || query.color() != null) {
            specification = specification.and(
                    ProductSpecifications.activeVariant(query.variantSize(), query.color())
            );
        }
        return productRepository.findAll(specification, pageable).map(ProductResult::from);
    }

    private static boolean isSlugUniqueViolation(Throwable failure) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = failure;

        while (current != null && visited.add(current)) {
            if (current instanceof ConstraintViolationException violation
                    && violation.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE
                    && isSlugConstraint(violation.getConstraintName())) {
                return true;
            }
            if (current instanceof SQLException sqlException
                    && sqlException.getErrorCode() == 1062
                    && "23000".equals(sqlException.getSQLState())
                    && containsSlugConstraint(sqlException.getMessage())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isSlugConstraint(String constraintName) {
        if (constraintName == null) {
            return false;
        }
        String normalized = constraintName
                .replace(String.valueOf((char) 96), "")
                .replace(String.valueOf((char) 34), "")
                .toLowerCase(Locale.ROOT);
        return normalized.equals("uk_products_slug")
                || normalized.endsWith(".uk_products_slug");
    }

    private static boolean containsSlugConstraint(String message) {
        return message != null && message.toLowerCase(Locale.ROOT).contains("uk_products_slug");
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ProductDetailResult getActiveDetail(UUID id) {
        ProductResult product = getActive(id);
        return new ProductDetailResult(
                product,
                productVariantService.listActiveForProduct(id),
                productImageService.listForProduct(id)
        );
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