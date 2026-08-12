package com.lyrashop.auth.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;

import com.lyrashop.auth.entity.RefreshTokenDigest;
import com.lyrashop.exception.InvalidCredentialsException;
import com.lyrashop.exception.InvalidRefreshTokenException;

@Service
public class RefreshTokenService {

    static final int MAX_TRANSACTION_ATTEMPTS = 3;

    private final RefreshSessionTransactions transactions;

    public RefreshTokenService(RefreshSessionTransactions transactions) {
        this.transactions = transactions;
    }

    public IssuedAuthentication issueInitial(UUID userId) {
        Objects.requireNonNull(userId, "userId");
        Optional<IssuedAuthentication> authentication =
                withTransientRetry(() -> transactions.issueInitial(userId));
        return authentication.orElseThrow(InvalidCredentialsException::new);
    }

    public IssuedAuthentication refresh(String rawRefreshToken) {
        RefreshTokenDigest tokenDigest = parse(rawRefreshToken);
        Optional<IssuedAuthentication> authentication =
                withTransientRetry(() -> transactions.rotate(tokenDigest));
        return authentication.orElseThrow(InvalidRefreshTokenException::new);
    }

    public void logout(UUID authenticatedUserId, String rawRefreshToken) {
        Objects.requireNonNull(authenticatedUserId, "authenticatedUserId");

        RefreshTokenDigest tokenDigest;
        try {
            tokenDigest = RefreshTokenDigest.fromRawToken(rawRefreshToken);
        } catch (IllegalArgumentException exception) {
            return;
        }

        withTransientRetry(() -> {
            transactions.logout(authenticatedUserId, tokenDigest);
            return null;
        });
    }

    private static RefreshTokenDigest parse(String rawRefreshToken) {
        try {
            return RefreshTokenDigest.fromRawToken(rawRefreshToken);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException();
        }
    }

    private static <T> T withTransientRetry(Supplier<T> transaction) {
        for (int attempt = 1; attempt <= MAX_TRANSACTION_ATTEMPTS; attempt++) {
            try {
                return transaction.get();
            } catch (TransientDataAccessException exception) {
                if (attempt == MAX_TRANSACTION_ATTEMPTS) {
                    throw exception;
                }
            }
        }
        throw new IllegalStateException("Transaction retry loop terminated unexpectedly");
    }
}
