package com.lyrashop.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.auth.dto.LoginRequest;
import com.lyrashop.auth.dto.LoginResponse;
import com.lyrashop.auth.dto.RegisterRequest;
import com.lyrashop.auth.dto.RegisterResponse;
import com.lyrashop.auth.service.LoginService;
import com.lyrashop.auth.service.RegistrationService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;

    public AuthController(
            RegistrationService registrationService,
            LoginService loginService
    ) {
        this.registrationService = registrationService;
        this.loginService = loginService;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(LoginResponse.from(loginService.login(request)));
    }
}
