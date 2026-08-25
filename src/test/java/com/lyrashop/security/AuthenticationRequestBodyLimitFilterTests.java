package com.lyrashop.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyrashop.exception.ApiErrorWriter;

import jakarta.servlet.http.HttpServletRequest;

class AuthenticationRequestBodyLimitFilterTests {

    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";
    private static final String ADMIN_CATEGORY_PATH = "/api/v1/admin/categories";
    private static final String ADMIN_PRODUCT_PATH = "/api/v1/admin/products";
    private static final String ADMIN_PRODUCT_DEACTIVATE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/deactivate";
    private static final String ADMIN_PRODUCT_UPDATE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000";
    private static final String ADMIN_VARIANT_UPDATE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/variants/11111111-1111-1111-1111-111111111111";
    private static final String ADMIN_VARIANT_DEACTIVATE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/variants/11111111-1111-1111-1111-111111111111/deactivate";
    private static final String ADMIN_VARIANT_INVENTORY_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/variants/11111111-1111-1111-1111-111111111111/inventory";
    private static final String ADMIN_PRODUCT_IMAGE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/images";
    private static final String CART_ITEMS_PATH = "/api/v1/cart/items";
    private static final String CART_ITEM_UPDATE_PATH = "/api/v1/cart/items/1";
    private static final String ORDERS_PATH = "/api/v1/orders";
    private static final String ADMIN_ORDER_STATUS_PATH = "/api/v1/admin/orders/00000000-0000-0000-0000-000000000000/status";
    private static final String PRODUCT_REVIEWS_PATH = "/api/v1/products/00000000-0000-0000-0000-000000000000/reviews";
    private static final String ADMIN_USER_STATUS_PATH = "/api/v1/admin/users/00000000-0000-0000-0000-000000000000/status";
    private static final int BODY_LIMIT = 16;
    private static final int BUSINESS_BODY_LIMIT = 32;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final AuthenticationRequestBodyLimitFilter filter =
            new AuthenticationRequestBodyLimitFilter(
                    BODY_LIMIT,
                    BUSINESS_BODY_LIMIT,
                    new ApiErrorWriter(objectMapper)
            );

    @Test
    void rejectsDeclaredOversizeWithoutReadingTheRequestStream() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public long getContentLengthLong() {
                return BODY_LIMIT + 1L;
            }

            @Override
            public jakarta.servlet.ServletInputStream getInputStream() {
                throw new AssertionError("oversized declared body must not be read");
            }
        };
        configure(request, "POST", REGISTER_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                chainCalled.set(true)
        );

        assertPayloadTooLarge(response, REGISTER_PATH);
        assertThat(chainCalled).isFalse();
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 1})
    void rejectsActualOversizeWhenTheDeclaredLengthIsMissingOrUnderstated(
            long declaredLength
    ) throws Exception {
        MockHttpServletRequest request = request(
                "x".repeat(BODY_LIMIT + 1).getBytes(StandardCharsets.UTF_8),
                declaredLength
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                chainCalled.set(true)
        );

        assertPayloadTooLarge(response, REGISTER_PATH);
        assertThat(chainCalled).isFalse();
    }

    @Test
    void measuresMultibyteBodiesByRawBytes() throws Exception {
        String threeByteCharacter = String.valueOf((char) 0x20ac);
        byte[] body = threeByteCharacter.repeat(6).getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = request(body, -1);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new AssertionError("oversized multibyte body must not reach the chain");
        });

        assertThat(body.length).isGreaterThan(BODY_LIMIT);
        assertPayloadTooLarge(response, REGISTER_PATH);
    }

    @Test
    void appliesTheSameRawBodyLimitToLogin() throws Exception {
        byte[] body = "x".repeat(BODY_LIMIT + 1).getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = request(body, body.length, LOGIN_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new AssertionError("oversized login body must not reach the chain");
        });

        assertPayloadTooLarge(response, LOGIN_PATH);
    }

    @ParameterizedTest
    @ValueSource(strings = {REFRESH_PATH, LOGOUT_PATH})
    void appliesTheSameRawBodyLimitToTokenCookieEndpoints(String path) throws Exception {
        byte[] body = "x".repeat(BODY_LIMIT + 1).getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = request(body, body.length, path);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new AssertionError("oversized token-cookie body must not reach the chain");
        });

        assertPayloadTooLarge(response, path);
    }

    @ParameterizedTest
    @ValueSource(strings = {ADMIN_CATEGORY_PATH, ADMIN_PRODUCT_PATH, ADMIN_PRODUCT_DEACTIVATE_PATH, ADMIN_PRODUCT_UPDATE_PATH, ADMIN_VARIANT_UPDATE_PATH, ADMIN_VARIANT_DEACTIVATE_PATH, ADMIN_VARIANT_INVENTORY_PATH, ADMIN_PRODUCT_IMAGE_PATH, CART_ITEMS_PATH, CART_ITEM_UPDATE_PATH, ORDERS_PATH, ADMIN_ORDER_STATUS_PATH, PRODUCT_REVIEWS_PATH, ADMIN_USER_STATUS_PATH})
    void appliesBusinessBodyLimitToAdminCreation(String path) throws Exception {
        byte[] body = "x".repeat(BUSINESS_BODY_LIMIT + 1).getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = request(body, body.length, path);
        if (ADMIN_PRODUCT_DEACTIVATE_PATH.equals(path) || ADMIN_VARIANT_DEACTIVATE_PATH.equals(path) || ADMIN_VARIANT_INVENTORY_PATH.equals(path)) {
            request.setMethod("PATCH");
        } else if (ADMIN_PRODUCT_UPDATE_PATH.equals(path) || ADMIN_VARIANT_UPDATE_PATH.equals(path) || CART_ITEM_UPDATE_PATH.equals(path) || ADMIN_ORDER_STATUS_PATH.equals(path) || ADMIN_USER_STATUS_PATH.equals(path)) {
            request.setMethod("PUT");
        }
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new AssertionError("oversized business body must not reach the chain");
        });

        assertPayloadTooLarge(response, path);
    }

    @Test
    void passesAnExactLimitBodyWithoutConsumingIt() throws Exception {
        byte[] body = "x".repeat(BODY_LIMIT).getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = request(body, body.length);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<HttpServletRequest> chainedRequest = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                chainedRequest.set((HttpServletRequest) servletRequest)
        );

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chainedRequest).hasValueSatisfying(cachedRequest -> {
            try {
                assertThat(cachedRequest.getInputStream().readAllBytes()).isEqualTo(body);
                assertThat(cachedRequest.getContentLengthLong()).isEqualTo(BODY_LIMIT);
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        });
    }

    @Test
    void leavesOtherMethodsAndPathsUntouched() throws Exception {
        byte[] oversizedBody = "x".repeat(BODY_LIMIT + 1).getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest otherMethod = request(oversizedBody, oversizedBody.length);
        otherMethod.setMethod("GET");
        assertBypassesFilter(otherMethod);

        MockHttpServletRequest otherPath = request(oversizedBody, oversizedBody.length);
        otherPath.setRequestURI("/api/v1/auth/csrf");
        otherPath.setServletPath("/api/v1/auth/csrf");
        assertBypassesFilter(otherPath);

        MockHttpServletRequest publicPath = request(oversizedBody, oversizedBody.length, "/api/v1/categories");
        assertBypassesFilter(publicPath);
    }

    private void assertPayloadTooLarge(
            MockHttpServletResponse response,
            String expectedPath
    ) throws Exception {
        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(MediaType.parseMediaType(response.getContentType())
                .isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)).isTrue();
        var body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(body.path("status").asInt()).isEqualTo(413);
        assertThat(body.path("code").asText()).isEqualTo("PAYLOAD_TOO_LARGE");
        assertThat(body.path("path").asText()).isEqualTo(expectedPath);
    }

    private static MockHttpServletRequest request(byte[] body, long declaredLength) {
        return request(body, declaredLength, REGISTER_PATH);
    }

    private static MockHttpServletRequest request(
            byte[] body,
            long declaredLength,
            String path
    ) {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public int getContentLength() {
                return declaredLength > Integer.MAX_VALUE ? -1 : (int) declaredLength;
            }

            @Override
            public long getContentLengthLong() {
                return declaredLength;
            }
        };
        configure(request, "POST", path);
        request.setContent(body);
        return request;
    }

    private void assertBypassesFilter(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Object> chainedRequest = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                chainedRequest.set(servletRequest)
        );

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chainedRequest.get()).isSameAs(request);
    }

    private static void configure(MockHttpServletRequest request, String method, String path) {
        request.setMethod(method);
        request.setRequestURI(path);
        request.setServletPath(path);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
