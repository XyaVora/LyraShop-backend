package com.lyrashop.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.lyrashop.auth.service.RefreshCookieService;
import com.lyrashop.exception.ApiErrorWriter;
import com.lyrashop.security.AuthenticationRequestBodyLimitFilter;
import com.lyrashop.security.RestSecurityErrorHandler;
import com.lyrashop.user.entity.UserRole;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({
        CorsProperties.class,
        AuthProtectionProperties.class,
        BootstrapAdminProperties.class
})
public class SecurityConfig {

    private static final String BCRYPT_ID = "bcrypt";
    private static final int BCRYPT_STRENGTH = 12;
    static final RequestMatcher COOKIE_CSRF_REQUEST = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/auth/refresh"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/auth/logout")
    );
    public static final String XSRF_COOKIE_NAME = RefreshCookieService.CSRF_COOKIE_NAME;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestSecurityErrorHandler securityErrorHandler,
            ApiErrorWriter errorWriter,
            AuthProtectionProperties authProtectionProperties,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CookieCsrfTokenRepository csrfTokenRepository,
            CsrfTokenRequestAttributeHandler csrfTokenRequestHandler
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .addFilterAfter(
                        new AuthenticationRequestBodyLimitFilter(
                                authProtectionProperties.maxRequestBodyBytes(),
                                authProtectionProperties.businessRequestBodyBytes(),
                                errorWriter
                        ),
                        CorsFilter.class
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                        .requireCsrfProtectionMatcher(COOKIE_CSRF_REQUEST))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/categories",
                                "/api/v1/categories/*"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/{productId}/reviews").permitAll()
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole(UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/cart", "/api/v1/cart/**")
                        .hasAnyRole(UserRole.CUSTOMER.name(), UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/orders", "/api/v1/orders/**")
                        .hasAnyRole(UserRole.CUSTOMER.name(), UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/me")
                        .hasAnyRole(UserRole.CUSTOMER.name(), UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/{productId}/reviews")
                        .hasAnyRole(UserRole.CUSTOMER.name(), UserRole.ADMIN.name())
                        .anyRequest().denyAll()
                );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(
                BCRYPT_ID,
                new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2B, BCRYPT_STRENGTH)
        );
        return new DelegatingPasswordEncoder(BCRYPT_ID, encoders);
    }

    @Bean
    CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        repository.setCookieName(XSRF_COOKIE_NAME);
        repository.setHeaderName(RefreshCookieService.XSRF_HEADER_NAME);
        repository.setCookieCustomizer(cookie -> cookie
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(RefreshCookieService.COOKIE_PATH));
        return repository;
    }

    @Bean
    CsrfTokenRequestAttributeHandler csrfTokenRequestHandler() {
        return new CsrfTokenRequestAttributeHandler();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name()
        ));
        configuration.setAllowedHeaders(List.of(
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                RefreshCookieService.XSRF_HEADER_NAME
        ));
        configuration.setExposedHeaders(List.of(
                HttpHeaders.RETRY_AFTER,
                RefreshCookieService.XSRF_HEADER_NAME
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
