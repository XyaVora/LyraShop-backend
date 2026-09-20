package com.lyrashop.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import com.lyrashop.auth.controller.AuthController;
import com.lyrashop.auth.dto.RegisterRequest;
import com.lyrashop.auth.service.LoginService;
import com.lyrashop.auth.service.PasswordService;
import com.lyrashop.auth.service.PasswordResetService;
import com.lyrashop.auth.service.RefreshCookieService;
import com.lyrashop.auth.service.RefreshTokenService;
import com.lyrashop.auth.service.RegistrationService;

import jakarta.servlet.http.Cookie;

class GlobalExceptionHandlerTests {

    private final RegistrationService registrationService = mock(RegistrationService.class);
    private final LoginService loginService = mock(LoginService.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final RefreshCookieService refreshCookieService = new RefreshCookieService();
    private final CsrfTokenRepository csrfTokenRepository = mock(CsrfTokenRepository.class);
    private final PasswordService passwordService = mock(PasswordService.class);
    private final PasswordResetService passwordResetService = mock(PasswordResetService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(
                    registrationService,
                    loginService,
                    refreshTokenService,
                    refreshCookieService,
                    csrfTokenRepository,
                    passwordService,
                    passwordResetService
            ))
            .setControllerAdvice(new GlobalExceptionHandler(refreshCookieService))
            .build();

    @Test
    void returnsRetryGuidanceWhenAuthenticationCapacityIsExhausted() throws Exception {
        String rejectedPassword = "valid-password-material";
        when(registrationService.register(any(RegisterRequest.class)))
                .thenThrow(new AuthenticationCapacityExceededException(3));

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "capacity@example.com",
                                  "password": "%s",
                                  "fullName": "Capacity Test",
                                  "phone": null
                                }
                                """.formatted(rejectedPassword)))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "3"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_BUSY"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain(rejectedPassword, "capacity@example.com");
    }

    @Test
    void returnsGenericInvalidRefreshProblemAndClearsTheRefreshCookie() throws Exception {
        String rejectedToken = "invalid-refresh-token";
        when(refreshTokenService.refresh(eq(rejectedToken)))
                .thenThrow(new InvalidRefreshTokenException());

        var result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie(RefreshCookieService.COOKIE_NAME, rejectedToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString(
                                        RefreshCookieService.COOKIE_NAME + "="
                                ),
                                org.hamcrest.Matchers.containsString("Max-Age=0"),
                                org.hamcrest.Matchers.containsString("Secure"),
                                org.hamcrest.Matchers.containsString("HttpOnly"),
                                org.hamcrest.Matchers.containsString("SameSite=Strict")
                        )
                ))
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message").value("Refresh token is invalid"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/refresh"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain(rejectedToken);
    }
}
