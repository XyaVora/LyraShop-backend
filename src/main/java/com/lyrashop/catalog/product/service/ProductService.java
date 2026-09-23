package com.lyrashop.catalog.product.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.catalog.category.entity.Category;
import com.lyrashop.catalog.category.repository.CategoryRepository;
import com.lyrashop.catalog.product.dto.AdminProductDetailResponse;
import com.lyrashop.catalog.product.dto.AdminProductResponse;
import com.lyrashop.catalog.product.dto.CreateProductRequest;
import com.lyrashop.catalog.product.dto.UpdateProductRequest;
import com.lyrashop.catalog.product.dto.ProductResponse;
import com.lyrashop.catalog.product.dto.ProductFacetsResponse;
import com.lyrashop.catalog.product.entity.Product;
import com.lyrashop.catalog.product.entity.ProductImage;
import com.lyrashop.catalog.product.repository.ProductImageRepository;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.entity.ProductVariant;
import com.lyrashop.catalog.variant.repository.ProductVariantRepository;
import com.lyrashop.catalog.variant.service.ProductVariantService;
import com.lyrashop.review.repository.ProductReviewSummary;
import com.lyrashop.review.repository.ReviewRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantService productVariantService;
    private final ProductImageService productImageService;
    private final ReviewRepository reviews;
    private final ProductVariantRepository variants;
    private final ProductImageRepository images;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductVariantService productVariantService,
            ProductImageService productImageService,
            ReviewRepository reviews,
            ProductVariantRepository variants,
            ProductImageRepository images
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantService = productVariantService;
        this.productImageService = productImageService;
        this.reviews = reviews;
        this.variants = variants;
        this.images = images;
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

    @Transactional
    public void activate(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        product.activate();
        productRepository.saveAndFlush(product);
    }

    @Transactional(readOnly = true)
    public List<AdminProductResponse> listForAdmin() {
        return productRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(AdminProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProductDetailResponse getForAdmin(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        return AdminProductDetailResponse.from(
                product,
                productVariantService.listForAdmin(id),
                productImageService.listForProduct(id)
        );
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
        Page<Product> products = productRepository.findAll(specification, pageable);
        Map<UUID, ProductReviewSummary> summaries = summariesFor(
                products.getContent().stream().map(Product::getId).toList()
        );
        return products.map(product -> withSummary(product, summaries));
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
                .map(product -> withSummary(product, summariesFor(List.of(product.getId()))))
                .orElseThrow(ProductNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<ProductResult> related(UUID id, int size) {
        Product product = productRepository.findOne(ProductSpecifications.id(id)
                        .and(ProductSpecifications.active())
                        .and(ProductSpecifications.categoryActive()))
                .orElseThrow(ProductNotFoundException::new);
        int limit = Math.max(1, Math.min(size, 12));
        var matches = productRepository.findAll(
                ProductSpecifications.active()
                        .and(ProductSpecifications.categoryActive())
                        .and(ProductSpecifications.categoryId(product.getCategoryId()))
                        .and(ProductSpecifications.idNot(id)),
                PageRequest.of(0, limit, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")))
        ).getContent();
        Map<UUID, ProductReviewSummary> summaries = summariesFor(matches.stream().map(Product::getId).toList());
        return matches.stream().map(item -> withSummary(item, summaries)).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResult> featured(int size) {
        int limit = Math.max(1, Math.min(size, 20));
        var candidates = productRepository.findAll(
                ProductSpecifications.active().and(ProductSpecifications.categoryActive()),
                PageRequest.of(0, 100, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")))
        ).getContent();
        Map<UUID, ProductReviewSummary> summaries = summariesFor(candidates.stream().map(Product::getId).toList());
        return candidates.stream()
                .map(item -> withSummary(item, summaries))
                .sorted(Comparator.comparingLong(ProductResult::reviewCount).reversed()
                        .thenComparing(ProductResult::averageRating,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ProductResult::createdAt, Comparator.reverseOrder()))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductFacetsResponse facets() {
        return new ProductFacetsResponse(variants.findPublicColors(), variants.findPublicSizes());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> publicResponses(List<ProductResult> products) {
        if (products.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = products.stream().map(ProductResult::id).toList();
        Map<UUID, List<ProductVariant>> variantsByProduct = variants
                .findAllByProductIdInAndActiveTrueOrderByProductIdAscSkuAscIdAsc(ids)
                .stream().collect(Collectors.groupingBy(ProductVariant::getProductId));
        Map<UUID, List<ProductImage>> imagesByProduct = images
                .findAllByProductIdInOrderByProductIdAscSortOrderAscIdAsc(ids)
                .stream().collect(Collectors.groupingBy(ProductImage::getProductId));
        return products.stream()
                .map(product -> ProductResponse.from(
                        product,
                        variantsByProduct.getOrDefault(product.id(), List.of()),
                        imagesByProduct.getOrDefault(product.id(), List.of())
                ))
                .toList();
    }

    private Map<UUID, ProductReviewSummary> summariesFor(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return reviews.summarizeByProductIds(productIds).stream()
                .collect(Collectors.toMap(ProductReviewSummary::getProductId, summary -> summary));
    }

    private static ProductResult withSummary(Product product, Map<UUID, ProductReviewSummary> summaries) {
        ProductReviewSummary summary = summaries.get(product.getId());
        if (summary == null || summary.getReviewCount() == null || summary.getReviewCount() == 0L) {
            return ProductResult.from(product, null, 0);
        }
        BigDecimal average = summary.getAverageRating() == null
                ? null
                : BigDecimal.valueOf(summary.getAverageRating()).setScale(2, RoundingMode.HALF_UP);
        return ProductResult.from(product, average, summary.getReviewCount());
    }
}
