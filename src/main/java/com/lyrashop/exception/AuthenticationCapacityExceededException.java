package com.lyrashop.exception;

public class AuthenticationCapacityExceededException extends RuntimeException {

    private final int retryAfterSeconds;

    public AuthenticationCapacityExceededException(int retryAfterSeconds) {
        super("Authentication capacity is temporarily exhausted");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
