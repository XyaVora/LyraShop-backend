package com.lyrashop.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyrashop.exception.ApiErrorWriter;

class RestSecurityErrorHandlerTests {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final RestSecurityErrorHandler errorHandler = new RestSecurityErrorHandler(
            new ApiErrorWriter(objectMapper)
    );

    @Test
    void returnsStableCsrfProblemWithoutBearerChallenge() throws Exception {
        MockHttpServletRequest request = request("/api/v1/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();

        errorHandler.handle(request, response, new MissingCsrfTokenException(null));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(MediaType.parseMediaType(response.getContentType())
                .isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)).isTrue();
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).isNull();
        var body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(body.path("code").asText()).isEqualTo("CSRF_REQUIRED");
        assertThat(body.path("message").asText()).isEqualTo("A valid CSRF token is required");
        assertThat(body.path("path").asText()).isEqualTo("/api/v1/auth/refresh");
    }

    @Test
    void keepsNonCsrfAuthorizationFailuresGeneric() throws Exception {
        MockHttpServletRequest request = request("/api/v1/private");
        MockHttpServletResponse response = new MockHttpServletResponse();

        errorHandler.handle(request, response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        var body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(body.path("code").asText()).isEqualTo("FORBIDDEN");
    }

    private static MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        request.setServletPath(path);
        return request;
    }
}
