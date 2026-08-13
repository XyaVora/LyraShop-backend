package com.lyrashop.catalog.product.service;

public final class ProductCategoryNotFoundException extends RuntimeException {

    public ProductCategoryNotFoundException() {
        super("Product category was not found");
    }
}
