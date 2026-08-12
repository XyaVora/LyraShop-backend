package com.lyrashop.exception;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid");
    }

    public InvalidRefreshTokenException(Throwable cause) {
        super("Refresh token is invalid", cause);
    }
}
