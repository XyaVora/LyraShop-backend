package com.lyrashop.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException() {
        super("Email is already registered");
    }

    public EmailAlreadyRegisteredException(Throwable cause) {
        super("Email is already registered", cause);
    }
}
