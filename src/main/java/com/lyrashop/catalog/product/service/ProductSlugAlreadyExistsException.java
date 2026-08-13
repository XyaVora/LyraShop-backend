package com.lyrashop.catalog.product.service;

public final class ProductSlugAlreadyExistsException extends RuntimeException {

    public ProductSlugAlreadyExistsException() {
        super("Product slug already exists");
    }

    public ProductSlugAlreadyExistsException(Throwable cause) {
        super("Product slug already exists", cause);
    }
}
