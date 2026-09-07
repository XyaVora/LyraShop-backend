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
import com.lyrashop.catalog.product.service.InvalidProductImageUrlException;
import com.lyrashop.catalog.product.service.ProductVersionConflictException;

import com.lyrashop.review.service.ReviewAlreadyExistsException;
import com.lyrashop.review.service.ReviewNotAllowedException;
import com.lyrashop.review.service.ReviewNotFoundException;
import com.lyrashop.user.service.InvalidProfileDataException;
import com.lyrashop.user.service.LastAdminException;
import com.lyrashop.user.service.UserNotFoundException;
import com.lyrashop.order.service.EmptyCartException;
import com.lyrashop.order.service.InvalidOrderStatusException;
import com.lyrashop.order.service.InvalidPaymentMethodException;
import com.lyrashop.order.service.OrderNotFoundException;
import com.lyrashop.cart.service.CartItemNotFoundException;
import com.lyrashop.cart.service.InsufficientStockException;
import com.lyrashop.catalog.variant.service.VariantProductNotFoundException;
import com.lyrashop.catalog.variant.service.VariantSkuAlreadyExistsException;
import com.lyrashop.catalog.variant.service.VariantNotFoundException;
import com.lyrashop.catalog.variant.service.VariantVersionConflictException;

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

    @ExceptionHandler(InvalidProductImageUrlException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidProductImageUrl(
            InvalidProductImageUrlException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_PRODUCT_IMAGE_URL",
                        "Product image URL is invalid",
                        request.getRequestURI()
                )
        );
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

    @ExceptionHandler(ReviewNotAllowedException.class)
    ResponseEntity<ApiErrorResponse> handleReviewNotAllowed(
            ReviewNotAllowedException exception,
            HttpServletRequest request
    ) {
        return problem(HttpStatus.FORBIDDEN, ApiErrorResponse.of(
                HttpStatus.FORBIDDEN.value(), "REVIEW_NOT_ALLOWED",
                "Review is only allowed after a delivered purchase", request.getRequestURI()));
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> handleReviewExists(
            ReviewAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return problem(HttpStatus.CONFLICT, ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(), "REVIEW_ALREADY_EXISTS",
                "A review for this product already exists", request.getRequestURI()));
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleReviewNotFound(
            ReviewNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(HttpStatus.NOT_FOUND, ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "REVIEW_NOT_FOUND", "Review was not found",
                request.getRequestURI()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(HttpStatus.NOT_FOUND, ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "USER_NOT_FOUND", "User was not found",
                request.getRequestURI()));
    }

    @ExceptionHandler(InvalidProfileDataException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidProfile(
            InvalidProfileDataException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorResponse.withFieldErrors(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_FAILED",
                        "Request validation failed",
                        request.getRequestURI(),
                        Map.of("fullName", "must be valid")
                )
        );
    }

    @ExceptionHandler(LastAdminException.class)
    ResponseEntity<ApiErrorResponse> handleLastAdmin(LastAdminException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "LAST_ADMIN",
                "The last remaining admin cannot be demoted",
                request.getRequestURI()));
    }

    @ExceptionHandler(EmptyCartException.class)
    ResponseEntity<ApiErrorResponse> handleEmptyCart(EmptyCartException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "EMPTY_CART", "Cart has no items", request.getRequestURI()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleOrderNotFound(OrderNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "ORDER_NOT_FOUND", "Order was not found", request.getRequestURI()));
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidOrderStatus(
            InvalidOrderStatusException exception,
            HttpServletRequest request
    ) {
        return problem(HttpStatus.CONFLICT, ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(), "INVALID_ORDER_STATUS", "Order status transition is not allowed",
                request.getRequestURI()));
    }

    @ExceptionHandler(InvalidPaymentMethodException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidPaymentMethod(
            InvalidPaymentMethodException exception,
            HttpServletRequest request
    ) {
        return problem(HttpStatus.BAD_REQUEST, ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "INVALID_PAYMENT_METHOD", "Payment method is not supported",
                request.getRequestURI()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    ResponseEntity<ApiErrorResponse> handleInsufficientStock(
            InsufficientStockException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "INSUFFICIENT_STOCK",
                        "Requested quantity exceeds available stock",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleCartItemNotFound(
            CartItemNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.NOT_FOUND,
                ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "CART_ITEM_NOT_FOUND",
                        "Cart item was not found",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(VariantProductNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleVariantProductNotFound(VariantProductNotFoundException e, HttpServletRequest r){return problem(HttpStatus.NOT_FOUND,ApiErrorResponse.of(404,"VARIANT_PRODUCT_NOT_FOUND","Variant product was not found",r.getRequestURI()));}
    @ExceptionHandler(VariantSkuAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> handleVariantSku(VariantSkuAlreadyExistsException e,HttpServletRequest r){return problem(HttpStatus.CONFLICT,ApiErrorResponse.of(409,"VARIANT_SKU_ALREADY_EXISTS","A variant with this SKU already exists",r.getRequestURI()));}
    @ExceptionHandler(VariantNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleVariantNotFound(VariantNotFoundException e,HttpServletRequest r){return problem(HttpStatus.NOT_FOUND,ApiErrorResponse.of(404,"VARIANT_NOT_FOUND","Variant was not found",r.getRequestURI()));}
    @ExceptionHandler(VariantVersionConflictException.class)
    ResponseEntity<ApiErrorResponse> handleVariantVersionConflict(VariantVersionConflictException e,HttpServletRequest r){return problem(HttpStatus.CONFLICT,ApiErrorResponse.of(409,"VARIANT_VERSION_CONFLICT","Variant was modified by another request",r.getRequestURI()));}

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
