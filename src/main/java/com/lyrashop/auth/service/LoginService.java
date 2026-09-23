package com.lyrashop.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.lyrashop.auth.dto.LoginRequest;
import com.lyrashop.auth.validation.PasswordMaterial;
import com.lyrashop.exception.InvalidCredentialsException;
import com.lyrashop.security.BoundedPasswordOperations;
import com.lyrashop.security.DummyPasswordHash;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class LoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);
    private static final String SAFE_INVALID_PASSWORD = "invalid-login-password";

    private final UserRepository userRepository;
    private final BoundedPasswordOperations passwordOperations;
    private final DummyPasswordHash dummyPasswordHash;
    private final RefreshTokenService refreshTokenService;

    public LoginService(
            UserRepository userRepository,
            BoundedPasswordOperations passwordOperations,
            DummyPasswordHash dummyPasswordHash,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordOperations = passwordOperations;
        this.dummyPasswordHash = dummyPasswordHash;
        this.refreshTokenService = refreshTokenService;
    }

    public IssuedAuthentication login(@NotNull @Valid LoginRequest request) {
        String canonicalEmail;
        try {
            canonicalEmail = User.canonicalizeEmail(request.email());
        } catch (IllegalArgumentException exception) {
            passwordOperations.matches(SAFE_INVALID_PASSWORD, dummyPasswordHash.value());
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(canonicalEmail).orElse(null);
        boolean passwordCompatible =
                PasswordMaterial.isBcryptCompatible(request.password());
        String candidate = passwordCompatible
                ? request.password()
                : SAFE_INVALID_PASSWORD;
        String encodedPassword = user == null
                ? dummyPasswordHash.value()
                : user.getPasswordHash();
        boolean passwordMatches = matches(candidate, encodedPassword, user);

        if (!passwordCompatible
                || user == null
                || !passwordMatches
                || !user.isActive()) {
            throw new InvalidCredentialsException();
        }
        if (!user.isEmailVerified()) throw new EmailNotVerifiedException();
        return refreshTokenService.issueInitial(user.getId());
    }

    private boolean matches(String candidate, String encodedPassword, User user) {
        try {
            return passwordOperations.matches(candidate, encodedPassword);
        } catch (IllegalArgumentException exception) {
            if (user != null) {
                LOGGER.warn("Stored password hash is invalid for user {}", user.getId());
            }
            return false;
        }
    }
}
