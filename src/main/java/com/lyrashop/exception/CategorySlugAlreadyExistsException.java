package com.lyrashop.exception;

public final class CategorySlugAlreadyExistsException extends RuntimeException {

    public CategorySlugAlreadyExistsException() {
        super("Category slug already exists");
    }

    public CategorySlugAlreadyExistsException(Throwable cause) {
        super("Category slug already exists", cause);
    }
}
