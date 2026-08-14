package com.lyrashop.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lyrashop.exception.ApiErrorResponse;
import com.lyrashop.exception.ApiErrorWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationRequestBodyLimitFilter extends OncePerRequestFilter {

    private static final RequestMatcher AUTHENTICATION_REQUEST = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/auth/register"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/auth/login"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/auth/refresh"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/auth/logout")
    );
    private static final RequestMatcher BUSINESS_REQUEST = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/admin/categories"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/admin/products"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.PUT, "/api/v1/admin/products/{id}"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/admin/products/{productId}/variants"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.PUT, "/api/v1/admin/products/{productId}/variants/{variantId}"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.PATCH, "/api/v1/admin/products/{id}/deactivate")
    );

    private final int authenticationMaxRequestBodyBytes;
    private final int businessMaxRequestBodyBytes;
    private final ApiErrorWriter errorWriter;

    public AuthenticationRequestBodyLimitFilter(
            int authenticationMaxRequestBodyBytes,
            int businessMaxRequestBodyBytes,
            ApiErrorWriter errorWriter
    ) {
        this.authenticationMaxRequestBodyBytes = authenticationMaxRequestBodyBytes;
        this.businessMaxRequestBodyBytes = businessMaxRequestBodyBytes;
        this.errorWriter = errorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !AUTHENTICATION_REQUEST.matches(request)
                && !BUSINESS_REQUEST.matches(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        int maxRequestBodyBytes = AUTHENTICATION_REQUEST.matches(request)
                ? authenticationMaxRequestBodyBytes
                : businessMaxRequestBodyBytes;
        if (request.getContentLengthLong() > maxRequestBodyBytes) {
            reject(request, response);
            return;
        }

        byte[] body = request.getInputStream().readNBytes(maxRequestBodyBytes + 1);
        if (body.length > maxRequestBodyBytes) {
            reject(request, response);
            return;
        }

        filterChain.doFilter(new CachedBodyRequest(request, body), response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        errorWriter.write(response, ApiErrorResponse.of(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "PAYLOAD_TOO_LARGE",
                "Request body exceeds the allowed size",
                request.getRequestURI()
        ));
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body.clone();
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(body);
        }

        @Override
        public BufferedReader getReader() {
            String encoding = getCharacterEncoding();
            Charset charset = encoding == null
                    ? StandardCharsets.UTF_8
                    : Charset.forName(encoding);
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
    }

    private static final class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream input;

        private CachedBodyServletInputStream(byte[] body) {
            this.input = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            return input.read();
        }

        @Override
        public int read(byte[] bytes, int offset, int length) {
            return input.read(bytes, offset, length);
        }

        @Override
        public boolean isFinished() {
            return input.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new IllegalStateException("Asynchronous reads are not supported");
        }
    }
}
