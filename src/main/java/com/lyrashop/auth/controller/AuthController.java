package com.lyrashop.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.auth.dto.LoginRequest;
import com.lyrashop.auth.dto.LoginResponse;
import com.lyrashop.auth.dto.RegisterRequest;
import com.lyrashop.auth.dto.RegisterResponse;
import com.lyrashop.auth.service.IssuedAuthentication;
import com.lyrashop.auth.service.LoginService;
import com.lyrashop.auth.service.RefreshCookieService;
import com.lyrashop.auth.service.RefreshTokenService;
import com.lyrashop.auth.service.RegistrationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    public static final String XSRF_HEADER_NAME = RefreshCookieService.XSRF_HEADER_NAME;

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieService refreshCookieService;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(
            RegistrationService registrationService,
            LoginService loginService,
            RefreshTokenService refreshTokenService,
            RefreshCookieService refreshCookieService,
            CsrfTokenRepository csrfTokenRepository
    ) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.refreshTokenService = refreshTokenService;
        this.refreshCookieService = refreshCookieService;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @PostMapping(
            path = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RegisterResponse.from(registrationService.register(request)));
    }

    @PostMapping(
            path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        IssuedAuthentication authentication = loginService.login(request);
        CsrfToken csrfToken = csrfTokenRepository.generateToken(servletRequest);
        csrfTokenRepository.saveToken(csrfToken, servletRequest, servletResponse);
        return tokenResponse(authentication)
                .header(RefreshCookieService.XSRF_HEADER_NAME, csrfToken.getToken())
                .body(LoginResponse.from(authentication.accessToken()));
    }

    @GetMapping(path = "/csrf")
    public ResponseEntity<Void> csrf(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        CsrfToken csrfToken = csrfTokenRepository.loadDeferredToken(request, response).get();
        return ResponseEntity.noContent()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(RefreshCookieService.XSRF_HEADER_NAME, csrfToken.getToken())
                .build();
    }

    @PostMapping(path = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        IssuedAuthentication authentication = refreshTokenService.refresh(
                refreshCookieService.read(request).orElse(null)
        );
        String csrfToken = currentCsrfToken(request, response).getToken();
        return tokenResponse(authentication)
                .header(RefreshCookieService.XSRF_HEADER_NAME, csrfToken)
                .body(LoginResponse.from(authentication.accessToken()));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        refreshTokenService.logout(
                UUID.fromString(authentication.getName()),
                refreshCookieService.read(request).orElse(null)
        );
        csrfTokenRepository.saveToken(null, request, response);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.clear().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .build();
    }

    private ResponseEntity.BodyBuilder tokenResponse(IssuedAuthentication authentication) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookieService.issue(authentication.refreshToken()).toString()
                )
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache");
    }

    private CsrfToken currentCsrfToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Object requestToken = request.getAttribute(CsrfToken.class.getName());
        if (requestToken instanceof CsrfToken csrfToken) {
            return csrfToken;
        }
        return csrfTokenRepository.loadDeferredToken(request, response).get();
    }
}
