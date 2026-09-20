package com.lyrashop.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.lyrashop.auth.dto.LoginRequest;
import com.lyrashop.auth.service.IssuedAccessToken;
import com.lyrashop.auth.service.IssuedAuthentication;
import com.lyrashop.auth.service.IssuedRefreshToken;
import com.lyrashop.auth.service.LoginService;
import com.lyrashop.auth.service.PasswordService;
import com.lyrashop.auth.service.PasswordResetService;
import com.lyrashop.auth.service.RefreshCookieService;
import com.lyrashop.auth.service.RefreshTokenService;
import com.lyrashop.auth.service.RegistrationService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class AuthControllerTests {

    private static final String CSRF_TOKEN_VALUE = "csrf-token-value";

    private final RegistrationService registrationService = mock(RegistrationService.class);
    private final LoginService loginService = mock(LoginService.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final RefreshCookieService refreshCookieService = new RefreshCookieService();
    private final CsrfTokenRepository csrfTokenRepository = mock(CsrfTokenRepository.class);
    private final PasswordService passwordService = mock(PasswordService.class);
    private final PasswordResetService passwordResetService = mock(PasswordResetService.class);
    private final CsrfToken csrfToken = new DefaultCsrfToken(
            RefreshCookieService.XSRF_HEADER_NAME,
            "_csrf",
            CSRF_TOKEN_VALUE
    );
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(
            registrationService,
            loginService,
            refreshTokenService,
            refreshCookieService,
            csrfTokenRepository,
            passwordService,
            passwordResetService
    )).build();

    @BeforeEach
    void configureCsrfRepository() {
        when(csrfTokenRepository.generateToken(any(HttpServletRequest.class)))
                .thenReturn(csrfToken);
        DeferredCsrfToken deferredToken = mock(DeferredCsrfToken.class);
        when(deferredToken.get()).thenReturn(csrfToken);
        when(csrfTokenRepository.loadDeferredToken(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        )).thenReturn(deferredToken);
    }

    @Test
    void loginRotatesCsrfAndSetsOnlyTheRefreshCookieAsCredentialOutput() throws Exception {
        IssuedAuthentication issued = authentication("initial-refresh-token");
        when(loginService.login(any(LoginRequest.class))).thenReturn(issued);

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "customer@example.com",
                                  "password": "opaque login password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(RefreshCookieService.XSRF_HEADER_NAME, CSRF_TOKEN_VALUE))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString(
                                        RefreshCookieService.COOKIE_NAME + "=initial-refresh-token"
                                ),
                                org.hamcrest.Matchers.containsString("Path=/api/v1/auth"),
                                org.hamcrest.Matchers.containsString("Secure"),
                                org.hamcrest.Matchers.containsString("HttpOnly"),
                                org.hamcrest.Matchers.containsString("SameSite=Strict")
                        )
                ))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("initial-refresh-token", CSRF_TOKEN_VALUE);
        verify(csrfTokenRepository).saveToken(
                eq(csrfToken),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    @Test
    void csrfBootstrapReturnsTheRawTokenWithoutAResponseBody() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(RefreshCookieService.XSRF_HEADER_NAME, CSRF_TOKEN_VALUE));

        verify(csrfTokenRepository).loadDeferredToken(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    @Test
    void refreshRotatesTheRefreshCookieAndReturnsTheCurrentRawCsrfToken() throws Exception {
        IssuedAuthentication issued = authentication("rotated-refresh-token");
        when(refreshTokenService.refresh("current-refresh-token")).thenReturn(issued);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie(
                                RefreshCookieService.COOKIE_NAME,
                                "current-refresh-token"
                        )))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(RefreshCookieService.XSRF_HEADER_NAME, CSRF_TOKEN_VALUE))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString(
                                RefreshCookieService.COOKIE_NAME + "=rotated-refresh-token"
                        )
                ))
                .andExpect(jsonPath("$.accessToken").value("access-token"));

        verify(refreshTokenService).refresh("current-refresh-token");
    }

    @Test
    void logoutUsesTheBearerSubjectAndClearsBothCredentialCookies() throws Exception {
        UUID userId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(),
                "unused",
                List.of()
        );

        mockMvc.perform(post("/api/v1/auth/logout")
                        .principal(authentication)
                        .cookie(new Cookie(
                                RefreshCookieService.COOKIE_NAME,
                                "current-refresh-token"
                        )))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString(
                                        RefreshCookieService.COOKIE_NAME + "="
                                ),
                                org.hamcrest.Matchers.containsString("Max-Age=0")
                        )
                ));

        verify(refreshTokenService).logout(userId, "current-refresh-token");
        verify(csrfTokenRepository).saveToken(
                isNull(),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    private static IssuedAuthentication authentication(String refreshToken) {
        return new IssuedAuthentication(
                new IssuedAccessToken("access-token", 900),
                new IssuedRefreshToken(
                        refreshToken,
                        Instant.parse("2026-08-19T00:00:00Z"),
                        604_800
                )
        );
    }
}
