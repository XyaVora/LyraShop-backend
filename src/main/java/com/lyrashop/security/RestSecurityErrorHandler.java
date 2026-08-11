package com.lyrashop.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.lyrashop.exception.ApiErrorResponse;
import com.lyrashop.exception.ApiErrorWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ApiErrorWriter errorWriter;

    public RestSecurityErrorHandler(ApiErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        errorWriter.write(response, ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Authentication is required",
                request.getRequestURI()
        ));
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException exception
    ) throws IOException, ServletException {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        errorWriter.write(response, ApiErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                "Access is denied",
                request.getRequestURI()
        ));
    }
}
