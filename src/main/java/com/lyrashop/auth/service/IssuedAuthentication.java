package com.lyrashop.auth.service;

import java.util.Objects;

public record IssuedAuthentication(
        IssuedAccessToken accessToken,
        IssuedRefreshToken refreshToken
) {

    public IssuedAuthentication {
        Objects.requireNonNull(accessToken, "accessToken");
        Objects.requireNonNull(refreshToken, "refreshToken");
    }

    @Override
    public String toString() {
        return "IssuedAuthentication[accessToken=[REDACTED], refreshToken=[REDACTED]]";
    }
}
