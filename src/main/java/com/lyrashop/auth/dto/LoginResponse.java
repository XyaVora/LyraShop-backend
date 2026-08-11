package com.lyrashop.auth.dto;

import com.lyrashop.auth.service.IssuedAccessToken;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

    private static final String BEARER = "Bearer";

    public static LoginResponse from(IssuedAccessToken token) {
        return new LoginResponse(token.value(), BEARER, token.expiresInSeconds());
    }

    @Override
    public String toString() {
        return "LoginResponse[accessToken=[REDACTED], tokenType=" + tokenType
                + ", expiresIn=" + expiresIn + "]";
    }
}
