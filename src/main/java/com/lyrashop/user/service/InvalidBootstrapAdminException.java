package com.lyrashop.user.service;

public class InvalidBootstrapAdminException extends IllegalStateException {

    public InvalidBootstrapAdminException(String message) {
        super(message);
    }
}
