package com.lyrashop.exception;

public class InvalidRegistrationDataException extends RuntimeException {

    private final String field;

    public InvalidRegistrationDataException(String field, Throwable cause) {
        super("Registration data is invalid", cause);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
