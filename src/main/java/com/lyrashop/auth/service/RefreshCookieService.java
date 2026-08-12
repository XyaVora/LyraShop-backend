package com.lyrashop.auth.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class RefreshCookieService {

    public static final String COOKIE_NAME = "__Secure-LyraShopRefresh";
    public static final String COOKIE_PATH = "/api/v1/auth";
    public static final String CSRF_COOKIE_NAME = "__Secure-LyraShopCsrf";
    public static final String XSRF_HEADER_NAME = "X-XSRF-TOKEN";

    public ResponseCookie issue(IssuedRefreshToken refreshToken) {
        return cookie(refreshToken.value(), Duration.ofSeconds(refreshToken.expiresInSeconds()));
    }

    public ResponseCookie clear() {
        return cookie("", Duration.ZERO);
    }

    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        String value = null;
        int matches = 0;
        for (Cookie cookie : cookies) {
            if (!COOKIE_NAME.equals(cookie.getName())) {
                continue;
            }
            matches++;
            if (matches > 1) {
                return Optional.empty();
            }
            value = cookie.getValue();
        }
        return Optional.ofNullable(value);
    }

    private static ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
