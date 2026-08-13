package com.lyrashop.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.lyrashop.auth.service.RefreshCookieService;

class SecurityConfigTests {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void configuresCredentialedCorsForTheCsrfBootstrapContract() {
        var source = securityConfig.corsConfigurationSource(
                new CorsProperties(List.of("https://shop.example.test"))
        );
        MockHttpServletRequest request = new MockHttpServletRequest(
                HttpMethod.GET.name(),
                "/api/v1/auth/csrf"
        );

        var configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins())
                .containsExactly("https://shop.example.test");
        assertThat(configuration.getAllowedMethods())
                .containsExactly(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.PUT.name()
                );
        assertThat(configuration.getAllowedHeaders()).contains(
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                RefreshCookieService.XSRF_HEADER_NAME
        );
        assertThat(configuration.getExposedHeaders()).contains(
                HttpHeaders.RETRY_AFTER,
                RefreshCookieService.XSRF_HEADER_NAME
        );
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    void configuresAHostOnlyHttpOnlySecureStrictCsrfCookie() {
        var repository = securityConfig.csrfTokenRepository();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        var token = repository.generateToken(request);
        repository.saveToken(token, request, response);

        var cookie = response.getCookie(RefreshCookieService.CSRF_COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo(token.getToken());
        assertThat(cookie.getPath()).isEqualTo(RefreshCookieService.COOKIE_PATH);
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getAttribute("SameSite")).isEqualTo("Strict");
        assertThat(cookie.getDomain()).isNull();

        MockHttpServletResponse clearResponse = new MockHttpServletResponse();
        repository.saveToken(null, request, clearResponse);
        var clearCookie = clearResponse.getCookie(RefreshCookieService.CSRF_COOKIE_NAME);
        assertThat(clearCookie).isNotNull();
        assertThat(clearCookie.getValue()).isEmpty();
        assertThat(clearCookie.getPath()).isEqualTo(RefreshCookieService.COOKIE_PATH);
        assertThat(clearCookie.getMaxAge()).isZero();
        assertThat(clearCookie.getSecure()).isTrue();
        assertThat(clearCookie.isHttpOnly()).isTrue();
        assertThat(clearCookie.getAttribute("SameSite")).isEqualTo("Strict");
        assertThat(clearCookie.getDomain()).isNull();
    }

    @Test
    void usesTheRawCsrfTokenRequestHandler() {
        assertThat(securityConfig.csrfTokenRequestHandler())
                .isExactlyInstanceOf(CsrfTokenRequestAttributeHandler.class);
    }

    @Test
    void requiresCsrfOnlyForCookieBackedAuthenticationMutations() {
        assertThat(SecurityConfig.COOKIE_CSRF_REQUEST.matches(request(
                HttpMethod.POST,
                "/api/v1/auth/refresh"
        ))).isTrue();
        assertThat(SecurityConfig.COOKIE_CSRF_REQUEST.matches(request(
                HttpMethod.POST,
                "/api/v1/auth/logout"
        ))).isTrue();
        assertThat(SecurityConfig.COOKIE_CSRF_REQUEST.matches(request(
                HttpMethod.POST,
                "/api/v1/admin/categories"
        ))).isFalse();
        assertThat(SecurityConfig.COOKIE_CSRF_REQUEST.matches(request(
                HttpMethod.POST,
                "/api/v1/auth/login"
        ))).isFalse();
        assertThat(SecurityConfig.COOKIE_CSRF_REQUEST.matches(request(
                HttpMethod.POST,
                "/api/v1/admin/products"
        ))).isFalse();
        assertThat(SecurityConfig.COOKIE_CSRF_REQUEST.matches(request(
                HttpMethod.GET,
                "/api/v1/auth/refresh"
        ))).isFalse();
    }

    @Test
    void enablesPrePostMethodAuthorization() {
        EnableMethodSecurity annotation = SecurityConfig.class.getAnnotation(
                EnableMethodSecurity.class
        );

        assertThat(annotation).isNotNull();
        assertThat(annotation.prePostEnabled()).isTrue();
    }

    private static MockHttpServletRequest request(HttpMethod method, String path) {
        return new MockHttpServletRequest(method.name(), path);
    }
}
