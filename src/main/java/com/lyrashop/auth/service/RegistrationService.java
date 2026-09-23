package com.lyrashop.auth.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.lyrashop.auth.dto.RegisterRequest;
import com.lyrashop.exception.EmailAlreadyRegisteredException;
import com.lyrashop.exception.InvalidRegistrationDataException;
import com.lyrashop.security.BoundedPasswordOperations;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class RegistrationService {

    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";
    private static final int MYSQL_DUPLICATE_KEY_ERROR = 1062;

    private final UserRepository userRepository;
    private final BoundedPasswordOperations passwordOperations;
    private final EmailVerificationService emailVerificationService;

    public RegistrationService(
            UserRepository userRepository,
            BoundedPasswordOperations passwordOperations,
            EmailVerificationService emailVerificationService
    ) {
        this.userRepository = userRepository;
        this.passwordOperations = passwordOperations;
        this.emailVerificationService = emailVerificationService;
    }

    public RegistrationResult register(@NotNull @Valid RegisterRequest request) {
        String canonicalEmail;
        try {
            canonicalEmail = User.canonicalizeEmail(request.email());
        } catch (IllegalArgumentException exception) {
            throw new InvalidRegistrationDataException("email", exception);
        }

        String passwordHash = passwordOperations.hash(request.password());
        if (userRepository.existsByEmail(canonicalEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = User.createUnverifiedCustomer(
                canonicalEmail,
                passwordHash,
                request.fullName(),
                request.phone()
        );

        try {
            User saved = userRepository.saveAndFlush(user);
            emailVerificationService.issue(saved, false);
            return RegistrationResult.from(saved);
        } catch (DataIntegrityViolationException exception) {
            if (isEmailUniqueViolation(exception)) {
                throw new EmailAlreadyRegisteredException(exception);
            }
            throw exception;
        }
    }

    private static boolean isEmailUniqueViolation(Throwable failure) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = failure;

        while (current != null && visited.add(current)) {
            if (current instanceof ConstraintViolationException violation
                    && violation.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE
                    && isEmailConstraint(violation.getConstraintName())) {
                return true;
            }
            if (current instanceof SQLException sqlException
                    && sqlException.getErrorCode() == MYSQL_DUPLICATE_KEY_ERROR
                    && "23000".equals(sqlException.getSQLState())
                    && containsEmailConstraint(sqlException.getMessage())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isEmailConstraint(String constraintName) {
        if (constraintName == null) {
            return false;
        }
        String normalized = constraintName
                .replace("`", "")
                .replace(String.valueOf((char) 34), "")
                .toLowerCase(Locale.ROOT);
        return normalized.equals(EMAIL_UNIQUE_CONSTRAINT)
                || normalized.endsWith("." + EMAIL_UNIQUE_CONSTRAINT);
    }

    private static boolean containsEmailConstraint(String message) {
        return message != null
                && message.toLowerCase(Locale.ROOT).contains(EMAIL_UNIQUE_CONSTRAINT);
    }
}
