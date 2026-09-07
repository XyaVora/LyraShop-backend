package com.lyrashop.user.service;

public class InvalidProfileDataException extends RuntimeException {

    public InvalidProfileDataException(Throwable cause) {
        super(cause);
    }
}
