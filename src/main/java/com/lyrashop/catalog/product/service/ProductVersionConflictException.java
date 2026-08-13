package com.lyrashop.catalog.product.service;

public final class ProductVersionConflictException extends RuntimeException {

    public ProductVersionConflictException() {
        super("Product version conflict");
    }

    public ProductVersionConflictException(Throwable cause) {
        super("Product version conflict", cause);
    }
}
