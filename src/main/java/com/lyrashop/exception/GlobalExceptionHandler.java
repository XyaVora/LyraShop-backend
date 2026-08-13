package com.lyrashop.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lyrashop.auth.service.RefreshCookieService;
import com.lyrashop.catalog.product.service.ProductCategoryNotFoundException;
import com.lyrashop.catalog.product.service.ProductNotFoundException;
import com.lyrashop.catalog.product.service.ProductQueryException;
import com.lyrashop.catalog.product.service.ProductSlugAlreadyExistsException;
import com.lyrashop.catalog.product.service.ProductVersionConflictException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final RefreshCookieService refreshCookieService;

    public GlobalExceptionHandler(RefreshCookieService refreshCookieService) {
        this.refreshCookieService = refreshCookieService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorResponse.withFieldErrors(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_FAILED",
                        "Request validation failed",
                        request.getRequestURI(),
                        fieldErrors
                )
        );
    }

    @ExceptionHandler(InvalidRegistrationDataException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidRegistration(
            InvalidRegistrationDataException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorResponse.withFieldErrors(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_FAILED",
                        "Request validation failed",
                        request.getRequestURI(),
                        Map.of(exception.getField(), "must be valid")
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "MALFORMED_REQUEST",
                        "Request body is malformed",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ApiErrorResponse.of(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                        "UNSUPPORTED_MEDIA_TYPE",
                        "Content type is not supported",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            EmailAlreadyRegisteredException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "EMAIL_ALREADY_REGISTERED",
                        "An account with this email already exists",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleProductNotFound(
            ProductNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.NOT_FOUND,
                ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND",
                        "Product was not found",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(ProductCategoryNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleProductCategoryNotFound(
            ProductCategoryNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.NOT_FOUND,
                ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_CATEGORY_NOT_FOUND",
                        "Product category was not found",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(ProductSlugAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateProductSlug(
            ProductSlugAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "PRODUCT_SLUG_ALREADY_EXISTS",
                        "A product with this slug already exists",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(ProductVersionConflictException.class)
    ResponseEntity<ApiErrorResponse> handleProductVersionConflict(
            ProductVersionConflictException exception,
            HttpServletRequest request
    ) {
        return problem(HttpStatus.CONFLICT, ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "PRODUCT_VERSION_CONFLICT",
                "Product was modified by another request",
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(ProductQueryException.class)
    ResponseEntity<ApiErrorResponse> handleProductQuery(
            ProductQueryException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_PRODUCT_QUERY",
                        "Product query parameters are invalid",
                        request.getRequestURI()
                )
        );
    }
    @ExceptionHandler(CategoryNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleCategoryNotFound(
            CategoryNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.NOT_FOUND,
                ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "CATEGORY_NOT_FOUND",
                        "Category was not found",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(CategorySlugAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateCategorySlug(
            CategorySlugAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "CATEGORY_SLUG_ALREADY_EXISTS",
                        "A category with this slug already exists",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(AuthenticationCapacityExceededException.class)
    ResponseEntity<ApiErrorResponse> handleAuthenticationCapacity(
            AuthenticationCapacityExceededException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Integer.toString(exception.getRetryAfterSeconds()))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ApiErrorResponse.of(
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "AUTHENTICATION_BUSY",
                        "Authentication is temporarily busy",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "INVALID_CREDENTIALS",
                        "Invalid email or password",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.clear().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "INVALID_REFRESH_TOKEN",
                        "Refresh token is invalid",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Unhandled request failure for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_ERROR",
                        "An unexpected error occurred",
                        request.getRequestURI()
                )
        );
    }

    private static ResponseEntity<ApiErrorResponse> problem(
            HttpStatus status,
            ApiErrorResponse body
    ) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
