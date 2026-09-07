package com.lyrashop.catalog.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import com.lyrashop.catalog.product.dto.CreateProductImageRequest;
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
}
