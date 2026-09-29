package com.lyrashop.catalog.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import com.lyrashop.catalog.product.dto.CreateProductImageRequest;
import com.lyrashop.catalog.product.dto.UpdateProductImageRequest;
import com.lyrashop.catalog.product.entity.ProductImage;
import com.lyrashop.catalog.product.repository.ProductImageRepository;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.repository.ProductVariantRepository;
import com.lyrashop.catalog.variant.service.VariantNotFoundException;

@Service
public class ProductImageService {

    private final ProductImageRepository images;
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ProductImageStorage storage;

    public ProductImageService(
            ProductImageRepository images,
            ProductRepository products,
            ProductVariantRepository variants,
            ProductImageStorage storage
    ) {
        this.images = images;
        this.products = products;
        this.variants = variants;
        this.storage = storage;
    }

    @Transactional
    public ProductImage create(UUID productId, CreateProductImageRequest request) {
        if (!products.existsById(productId)) {
            throw new ProductNotFoundException();
        }
        UUID variantId = request.variantId();
        if (variantId != null && variants.findByIdAndProductId(variantId, productId).isEmpty()) {
            throw new VariantNotFoundException();
        }
        String url;
        try {
            url = ProductImage.normalizeUrl(request.url());
        } catch (IllegalArgumentException exception) {
            throw new InvalidProductImageUrlException();
        }
        if (request.primary()) {
            images.findAllByProductIdAndPrimaryTrue(productId).forEach(ProductImage::clearPrimary);
        }
        return images.saveAndFlush(ProductImage.create(
                productId, variantId, url, request.primary(), request.sortOrder()
        ));
    }

    @Transactional
    public ProductImage createFromFile(
            UUID productId,
            MultipartFile file,
            UUID variantId,
            boolean primary,
            int sortOrder
    ) {
        return create(productId, new CreateProductImageRequest(
                storage.store(file),
                variantId,
                primary,
                sortOrder
        ));
    }

    @Transactional(readOnly = true)
    public List<ProductImage> listForProduct(UUID productId) {
        return images.findAllByProductIdOrderBySortOrderAscIdAsc(productId);
    }

    @Transactional
    public ProductImage update(UUID productId, Long imageId, UpdateProductImageRequest request) {
        ProductImage image = images.findByIdAndProductId(imageId, productId)
                .orElseThrow(ProductNotFoundException::new);
        if (request.variantId() != null
                && variants.findByIdAndProductId(request.variantId(), productId).isEmpty()) {
            throw new VariantNotFoundException();
        }
        if (request.primary()) {
            images.findAllByProductIdAndPrimaryTrue(productId).stream()
                    .filter(current -> !current.getId().equals(imageId))
                    .forEach(ProductImage::clearPrimary);
        }
        image.updatePlacement(request.variantId(), request.primary(), request.sortOrder());
        return images.saveAndFlush(image);
    }

    @Transactional
    public void delete(UUID productId, Long imageId) {
        ProductImage image = images.findByIdAndProductId(imageId, productId)
                .orElseThrow(ProductNotFoundException::new);
        boolean wasPrimary = image.isPrimary();
        String url = image.getUrl();
        images.delete(image);
        images.flush();
        if (wasPrimary) {
            images.findAllByProductIdOrderBySortOrderAscIdAsc(productId).stream().findFirst()
                    .ifPresent(next -> {
                        next.updatePlacement(next.getVariantId(), true, next.getSortOrder());
                        images.save(next);
                    });
        }
        if (url.startsWith("/api/v1/files/")) storage.delete(url);
    }
}
