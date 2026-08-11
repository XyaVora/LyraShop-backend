package com.lyrashop.auth.service;

public record IssuedAccessToken(
        String value,
        long expiresInSeconds
) {
}
