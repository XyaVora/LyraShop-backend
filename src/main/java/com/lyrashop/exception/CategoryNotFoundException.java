package com.lyrashop.exception;

public final class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException() {
        super("Category was not found");
    }
}
