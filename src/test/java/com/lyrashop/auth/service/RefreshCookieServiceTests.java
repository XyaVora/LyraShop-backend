package com.lyrashop.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.http.Cookie;

class RefreshCookieServiceTests {

    private final RefreshCookieService cookieService = new RefreshCookieService();

    @Test
    void issuesHostOnlyRefreshCookieWithProductionSecurityAttributes() {
        IssuedRefreshToken token = new IssuedRefreshToken(
                "refresh-token-value",
                Instant.parse("2026-08-19T00:00:00Z"),
                Duration.ofDays(7).toSeconds()
        );

        var cookie = cookieService.issue(token);

        assertThat(cookie.getName()).isEqualTo(RefreshCookieService.COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(token.value());
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo(RefreshCookieService.COOKIE_PATH);
        assertThat(cookie.getDomain()).isNull();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void clearsTheSameCookieScopeExactly() {
        var cookie = cookieService.clear();

        assertThat(cookie.getName()).isEqualTo(RefreshCookieService.COOKIE_NAME);
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo(RefreshCookieService.COOKIE_PATH);
        assertThat(cookie.getDomain()).isNull();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
    }

    @Test
    void rejectsAmbiguousDuplicateRefreshCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie(RefreshCookieService.COOKIE_NAME, "first"),
                new Cookie(RefreshCookieService.COOKIE_NAME, "second")
        );

        assertThat(cookieService.read(request)).isEmpty();
    }

    @Test
    void readsOneRefreshCookieWithoutInspectingOtherCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("unrelated", "value"),
                new Cookie(RefreshCookieService.COOKIE_NAME, "refresh-token")
        );

        assertThat(cookieService.read(request)).contains("refresh-token");
    }
}
