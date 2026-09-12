package com.lyrashop.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Timeout(value = 90, unit = TimeUnit.SECONDS)
@Execution(ExecutionMode.SAME_THREAD)
class NginxRegistrationIngressTests {

    private static final DockerImageName NGINX_IMAGE = DockerImageName.parse(
            "nginx:1.30.4-alpine@sha256:97d490c12ba55b4946b01546d1c3ed324e8d41ab1c9fcb2a616aa470620e5b46"
    );
    private static final Path CONFIG_TEMPLATE = Path.of(
            "deploy",
            "nginx",
            "templates",
            "default.conf.template"
    ).toAbsolutePath().normalize();
    private static final String API_HOST = "127.0.0.1";
    private static final String ALLOWED_ORIGIN = "https://shop.example.test";
    private static final String REGISTRATION_PATH = "/api/v1/auth/register";
    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";
    private static final String ADMIN_PRODUCT_PATH = "/api/v1/admin/products";
    private static final String ADMIN_VARIANT_DEACTIVATE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/variants/11111111-1111-1111-1111-111111111111/deactivate";
    private static final String ADMIN_PRODUCT_ACTIVATE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/activate";
    private static final String ADMIN_VARIANT_INVENTORY_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/variants/11111111-1111-1111-1111-111111111111/inventory";
    private static final String ADMIN_PRODUCT_IMAGE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/images";
    private static final String ADMIN_CATEGORY_UPDATE_PATH = "/api/v1/admin/categories/12";
    private static final String ADMIN_CATEGORY_DEACTIVATE_PATH = "/api/v1/admin/categories/12/deactivate";
    private static final String ADMIN_CATEGORY_ACTIVATE_PATH = "/api/v1/admin/categories/12/activate";
    private static final String ADMIN_VARIANT_ACTIVATE_PATH = "/api/v1/admin/products/00000000-0000-0000-0000-000000000000/variants/11111111-1111-1111-1111-111111111111/activate";
    private static final String ADMIN_USER_ROLE_PATH = "/api/v1/admin/users/00000000-0000-0000-0000-000000000000/role";
    private static final String ADMIN_DASHBOARD_PATH = "/api/v1/admin/dashboard";
    private static final String PROFILE_PATH = "/api/v1/me";
    private static final String VNPAY_IPN_PATH = "/api/v1/payments/vnpay/ipn";
    private static final String PRODUCT_FILE_PATH =
            "/api/v1/files/00000000-0000-0000-0000-000000000000.jpg";
    private static final int UPLOAD_MAX_REQUEST_BODY_BYTES = 2_097_152;
    private static final Network NETWORK = Network.newNetwork();
    private static final GenericContainer<?> UPSTREAM_A = upstream("a");
    private static final GenericContainer<?> UPSTREAM_B = upstream("b");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @BeforeAll
    static void startUpstreams() {
        Startables.deepStart(Stream.of(UPSTREAM_A, UPSTREAM_B)).join();
    }

    @AfterAll
    static void stopUpstreams() {
        Stream.of(UPSTREAM_A, UPSTREAM_B)
                .parallel()
                .filter(GenericContainer::isRunning)
                .forEach(GenericContainer::stop);
        NETWORK.close();
    }

    @Test
    void proxiesAndRejectsVariantDeactivationPathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> valid = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_DEACTIVATE_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_DEACTIVATE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_DEACTIVATE_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> encodedMatrixParameter = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_DEACTIVATE_PATH + "%3Bscope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
            assertThat(encodedMatrixParameter.statusCode()).isEqualTo(404);
            assertThat(trailingSlash.headers().firstValue("X-Upstream-Id")).isEmpty();
            assertThat(matrixParameter.headers().firstValue("X-Upstream-Id")).isEmpty();
            assertThat(encodedMatrixParameter.headers().firstValue("X-Upstream-Id")).isEmpty();
        }
    }

    @Test
    void proxiesAndRejectsProductActivationPathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> valid = send(
                    gateway,
                    "PATCH",
                    ADMIN_PRODUCT_ACTIVATE_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "PATCH",
                    ADMIN_PRODUCT_ACTIVATE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "PATCH",
                    ADMIN_PRODUCT_ACTIVATE_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
            assertThat(trailingSlash.headers().firstValue("X-Upstream-Id")).isEmpty();
            assertThat(matrixParameter.headers().firstValue("X-Upstream-Id")).isEmpty();
        }
    }

    @Test
    void proxiesAndRejectsInventoryAdjustmentPathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString("{\"stock\":5,\"version\":0}");
            HttpResponse<String> valid = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_INVENTORY_PATH,
                    body,
                    Map.of("Content-Type", "application/json")
            );
            HttpResponse<String> oversized = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_INVENTORY_PATH,
                    HttpRequest.BodyPublishers.ofByteArray(new byte[8193]),
                    Map.of("Content-Type", "application/json")
            );
            assertThat(oversized.statusCode()).isEqualTo(413);
            assertThat(oversized.headers().firstValue("X-Upstream-Id")).isEmpty();
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_INVENTORY_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_INVENTORY_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> encodedMatrixParameter = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_INVENTORY_PATH + "%3Bscope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
            assertThat(encodedMatrixParameter.statusCode()).isEqualTo(404);
            assertThat(trailingSlash.headers().firstValue("X-Upstream-Id")).isEmpty();
            assertThat(matrixParameter.headers().firstValue("X-Upstream-Id")).isEmpty();
            assertThat(encodedMatrixParameter.headers().firstValue("X-Upstream-Id")).isEmpty();
        }
    }

    @Test
    void proxiesAndRejectsProductImagePathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    "{\"url\":\"https://cdn.example.test/a.jpg\",\"primary\":true,\"sortOrder\":0}"
            );
            HttpResponse<String> valid = send(
                    gateway,
                    "POST",
                    ADMIN_PRODUCT_IMAGE_PATH,
                    body,
                    Map.of("Content-Type", "application/json")
            );
            HttpResponse<String> oversized = send(
                    gateway,
                    "POST",
                    ADMIN_PRODUCT_IMAGE_PATH,
                    HttpRequest.BodyPublishers.ofByteArray(new byte[UPLOAD_MAX_REQUEST_BODY_BYTES + 1]),
                    Map.of("Content-Type", "application/json")
            );
            assertThat(oversized.statusCode()).isEqualTo(413);
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "POST",
                    ADMIN_PRODUCT_IMAGE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "POST",
                    ADMIN_PRODUCT_IMAGE_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
        }
    }

    @Test
    void proxiesAndRejectsProductFilePathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> valid = send(
                    gateway,
                    "GET",
                    PRODUCT_FILE_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "GET",
                    PRODUCT_FILE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "GET",
                    PRODUCT_FILE_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
        }
    }

    @Test
    void proxiesAndRejectsCategoryUpdateAndDeactivatePathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> updated = send(
                    gateway,
                    "PUT",
                    ADMIN_CATEGORY_UPDATE_PATH,
                    HttpRequest.BodyPublishers.ofString(
                            "{\"name\":\"Renamed\",\"slug\":\"renamed\",\"description\":null}"
                    ),
                    Map.of("Content-Type", "application/json")
            );
            HttpResponse<String> deactivated = send(
                    gateway,
                    "PATCH",
                    ADMIN_CATEGORY_DEACTIVATE_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> activated = send(
                    gateway,
                    "PATCH",
                    ADMIN_CATEGORY_ACTIVATE_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> variantActivated = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_ACTIVATE_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> oversized = send(
                    gateway,
                    "PUT",
                    ADMIN_CATEGORY_UPDATE_PATH,
                    HttpRequest.BodyPublishers.ofByteArray(new byte[8193]),
                    Map.of("Content-Type", "application/json")
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "PATCH",
                    ADMIN_CATEGORY_DEACTIVATE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> activateTrailingSlash = send(
                    gateway,
                    "PATCH",
                    ADMIN_CATEGORY_ACTIVATE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> variantActivateTrailingSlash = send(
                    gateway,
                    "PATCH",
                    ADMIN_VARIANT_ACTIVATE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "PUT",
                    ADMIN_CATEGORY_UPDATE_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(updated.statusCode()).isEqualTo(200);
            assertThat(deactivated.statusCode()).isEqualTo(200);
            assertThat(activated.statusCode()).isEqualTo(200);
            assertThat(variantActivated.statusCode()).isEqualTo(200);
            assertThat(header(updated, "X-Upstream-Id")).isIn("a", "b");
            assertThat(oversized.statusCode()).isEqualTo(413);
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(activateTrailingSlash.statusCode()).isEqualTo(404);
            assertThat(variantActivateTrailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
        }
    }

    @Test
    void proxiesAndRejectsAdminUserRolePathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> valid = send(
                    gateway,
                    "PUT",
                    ADMIN_USER_ROLE_PATH,
                    HttpRequest.BodyPublishers.ofString("{\"role\":\"ADMIN\"}"),
                    Map.of("Content-Type", "application/json")
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "PUT",
                    ADMIN_USER_ROLE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "PUT",
                    ADMIN_USER_ROLE_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
        }
    }

    @Test
    void proxiesAndRejectsVnpayIpnPathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> valid = send(
                    gateway,
                    "GET",
                    VNPAY_IPN_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "GET",
                    VNPAY_IPN_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "GET",
                    VNPAY_IPN_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
        }
    }

    @Test
    void proxiesAndRejectsProfilePathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> valid = send(
                    gateway,
                    "GET",
                    PROFILE_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> updated = send(
                    gateway,
                    "PUT",
                    PROFILE_PATH,
                    HttpRequest.BodyPublishers.ofString("{\"fullName\":\"New Name\"}"),
                    Map.of("Content-Type", "application/json")
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "GET",
                    PROFILE_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "PUT",
                    PROFILE_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(updated.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
        }
    }

    @Test
    void proxiesAndRejectsDashboardPathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> valid = send(
                    gateway,
                    "GET",
                    ADMIN_DASHBOARD_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "GET",
                    ADMIN_DASHBOARD_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "GET",
                    ADMIN_DASHBOARD_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
            assertThat(trailingSlash.headers().firstValue("X-Upstream-Id")).isEmpty();
            assertThat(matrixParameter.headers().firstValue("X-Upstream-Id")).isEmpty();
        }
    }

    @Test
    void proxiesAndRejectsAdminProductListPathVariants() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> valid = send(
                    gateway,
                    "GET",
                    ADMIN_PRODUCT_PATH,
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> trailingSlash = send(
                    gateway,
                    "GET",
                    ADMIN_PRODUCT_PATH + "/",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            HttpResponse<String> matrixParameter = send(
                    gateway,
                    "GET",
                    ADMIN_PRODUCT_PATH + ";scope=other",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );

            assertThat(valid.statusCode()).isEqualTo(200);
            assertThat(header(valid, "X-Upstream-Id")).isIn("a", "b");
            assertThat(trailingSlash.statusCode()).isEqualTo(404);
            assertThat(matrixParameter.statusCode()).isEqualTo(404);
            assertThat(trailingSlash.headers().firstValue("X-Upstream-Id")).isEmpty();
            assertThat(matrixParameter.headers().firstValue("X-Upstream-Id")).isEmpty();
        }
    }

    @Test
    void rendersACompleteValidConfiguration() throws Exception {
        try (Gateway gateway = startGateway(Policy.productionDefaults())) {
            Container.ExecResult syntaxCheck = gateway.container().execInContainer("nginx", "-t");
            Container.ExecResult renderedConfig = gateway.container().execInContainer(
                    "cat",
                    "/etc/nginx/conf.d/default.conf"
            );
            Container.ExecResult fullConfig = gateway.container().execInContainer("nginx", "-T");

            assertThat(syntaxCheck.getExitCode()).isZero();
            assertThat(syntaxCheck.getStderr()).contains("test is successful");
            assertThat(renderedConfig.getExitCode()).isZero();
            assertThat(fullConfig.getExitCode()).isZero();
            assertThat(fullConfig.getStdout().lines().toList())
                    .noneMatch(line -> line.matches("\\s*listen\\s+80(?:\\s.*|;)")
                            || line.matches("\\s*listen\\s+\\[::\\]:80(?:\\s.*|;)"));
            assertThat(renderedConfig.getStdout())
                    .doesNotContain(
                            "${API_",
                            "${BACKEND_",
                            "${CORS_",
                            "${BUSINESS_",
                            "${LOGIN_",
                            "${LOGOUT_",
                            "${REFRESH_",
                            "${REGISTRATION_",
                            "${UPLOAD_",
                            "$http_cookie",
                            "$http_x_xsrf_token"
                    )
                    .contains(
                            "resolver 127.0.0.11 valid=10s ipv6=off;",
                            "server backend:8080 resolve;",
                            "rate=5r/m",
                            "rate=30r/m",
                            "zone=login_per_ip",
                            "zone=login_global",
                            "zone=refresh_per_ip",
                            "zone=refresh_global",
                            "zone=logout_per_ip",
                            "zone=logout_global",
                            "client_max_body_size 8192;",
                            "client_max_body_size 4096;",
                            "client_max_body_size 1024;",
                            "client_max_body_size 2097152;",
                            "location ~ ^/api/v1/admin/products/[0-9a-fA-F-]+/images$",
                            "location /api/v1/admin/",
                            "location ^~ /api/v1/files/",
                            "location = /api/v1/cart",
                            "location ^~ /api/v1/cart/",
                            "location = /api/v1/wishlist",
                            "location ^~ /api/v1/wishlist/",
                            "location = /api/v1/addresses",
                            "location ^~ /api/v1/addresses/",
                            "location = /api/v1/promotions/active",
                            "location = /api/v1/orders",
                            "location ^~ /api/v1/orders/",
                            "location ~ ^/api/v1/products/[0-9a-fA-F-]+/reviews$",
                            "location = /api/v1/auth/login",
                            "location = /api/v1/auth/refresh",
                            "location = /api/v1/auth/logout",
                            "location = /actuator",
                            "location ^~ /actuator/",
                            "$request_method",
                            "$uri",
                            "$binary_remote_addr",
                            "$remote_addr"
                    );
        }
    }

    @Test
    void sharesPerIpQuotaAcrossQueriesAndBackendReplicas() throws Exception {
        try (Gateway gateway = startGateway(Policy.perIpLimited())) {
            List<HttpResponse<String>> responses = sendConcurrentRegistrations(gateway, 12);
            List<HttpResponse<String>> accepted = responses.stream()
                    .filter(response -> response.statusCode() == 200)
                    .toList();
            List<HttpResponse<String>> rejected = responses.stream()
                    .filter(response -> response.statusCode() == 429)
                    .toList();

            assertThat(accepted).hasSizeBetween(2, 4);
            assertThat(rejected).hasSize(12 - accepted.size());
            assertThat(rejected).hasSizeGreaterThanOrEqualTo(8);
            assertThat(accepted)
                    .extracting(response -> header(response, "X-Upstream-Id"))
                    .contains("a", "b");
            assertThat(rejected)
                    .allSatisfy(response -> {
                        assertRegistrationRateLimit(response, ALLOWED_ORIGIN);
                        assertThat(response.headers().firstValue("X-Upstream-Id")).isEmpty();
                        assertThat(response.body()).doesNotContain("spoofed");
                    });

            HttpResponse<String> disallowedOrigin = send(
                    gateway,
                    "POST",
                    REGISTRATION_PATH,
                    HttpRequest.BodyPublishers.ofString("{}"),
                    Map.of("Origin", "https://attacker.example.test")
            );
            assertThat(disallowedOrigin.statusCode()).isEqualTo(429);
            assertThat(disallowedOrigin.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
        }
    }

    @Test
    void chargesOnlyNormalizedRegistrationPosts() throws Exception {
        try (Gateway gateway = startGateway(Policy.perIpLimited())) {
            for (int attempt = 0; attempt < 5; attempt++) {
                assertThat(send(
                        gateway,
                        "OPTIONS",
                        REGISTRATION_PATH,
                        HttpRequest.BodyPublishers.noBody(),
                        Map.of(
                                "Origin", ALLOWED_ORIGIN,
                                "Access-Control-Request-Method", "POST",
                                "Access-Control-Request-Headers", "content-type"
                        )
                ).statusCode()).isEqualTo(200);
                assertThat(send(
                        gateway,
                        "GET",
                        REGISTRATION_PATH,
                        HttpRequest.BodyPublishers.noBody(),
                        Map.of()
                ).statusCode()).isEqualTo(200);
                assertThat(send(
                        gateway,
                        "POST",
                        LOGIN_PATH,
                        HttpRequest.BodyPublishers.ofString("{}"),
                        Map.of("Content-Type", "application/json")
                ).statusCode()).isEqualTo(200);
            }

            HttpResponse<String> blockedManagementEndpoint = send(
                    gateway,
                    "GET",
                    "/actuator/health/readiness",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            );
            assertThat(blockedManagementEndpoint.statusCode()).isEqualTo(404);
            assertThat(blockedManagementEndpoint.headers().firstValue("X-Upstream-Id")).isEmpty();
            assertThat(send(
                    gateway,
                    "GET",
                    "/actuator",
                    HttpRequest.BodyPublishers.noBody(),
                    Map.of()
            ).statusCode()).isEqualTo(404);

            assertThat(sendRegistration(gateway, REGISTRATION_PATH + "/", Map.of()).statusCode())
                    .isEqualTo(404);
            assertThat(sendRegistration(gateway, REGISTRATION_PATH + ";scope=other", Map.of()).statusCode())
                    .isEqualTo(404);
            assertThat(sendRegistration(gateway, REGISTRATION_PATH + "%3Bscope=other", Map.of()).statusCode())
                    .isEqualTo(404);
            assertThat(sendRegistration(gateway, LOGIN_PATH + "/", Map.of()).statusCode())
                    .isEqualTo(404);
            assertThat(sendRegistration(gateway, LOGIN_PATH + ";scope=other", Map.of()).statusCode())
                    .isEqualTo(404);
            assertThat(sendRegistration(gateway, LOGIN_PATH + "%3Bscope=other", Map.of()).statusCode())
                    .isEqualTo(404);
            for (String path : List.of(REFRESH_PATH, LOGOUT_PATH)) {
                assertThat(sendRegistration(gateway, path + "/", Map.of()).statusCode())
                        .isEqualTo(404);
                assertThat(sendRegistration(gateway, path + ";scope=other", Map.of()).statusCode())
                        .isEqualTo(404);
                assertThat(sendRegistration(gateway, path + "%3Bscope=other", Map.of()).statusCode())
                        .isEqualTo(404);
            }

            assertThat(List.of(
                    sendRegistration(gateway, REGISTRATION_PATH, Map.of()).statusCode(),
                    sendRegistration(gateway, "/api//v1/auth/register", Map.of()).statusCode(),
                    sendRegistration(gateway, REGISTRATION_PATH + "?attempt=query", Map.of()).statusCode(),
                    sendRegistration(gateway, REGISTRATION_PATH, Map.of()).statusCode()
            )).containsExactly(200, 200, 200, 429);
        }
    }

    @Test
    void appliesAnIndependentQuotaToOnlyNormalizedLoginPosts() throws Exception {
        try (Gateway gateway = startGateway(Policy.loginPerIpLimited())) {
            for (int attempt = 0; attempt < 5; attempt++) {
                assertThat(sendRegistration(
                        gateway,
                        REGISTRATION_PATH,
                        Map.of()
                ).statusCode()).isEqualTo(200);
                assertThat(send(
                        gateway,
                        "OPTIONS",
                        LOGIN_PATH,
                        HttpRequest.BodyPublishers.noBody(),
                        Map.of(
                                "Origin", ALLOWED_ORIGIN,
                                "Access-Control-Request-Method", "POST",
                                "Access-Control-Request-Headers", "content-type"
                        )
                ).statusCode()).isEqualTo(200);
                assertThat(send(
                        gateway,
                        "GET",
                        LOGIN_PATH,
                        HttpRequest.BodyPublishers.noBody(),
                        Map.of()
                ).statusCode()).isEqualTo(200);
            }

            List<HttpResponse<String>> attempts = List.of(
                    sendRegistration(gateway, LOGIN_PATH, Map.of()),
                    sendRegistration(gateway, "/api//v1/auth/login", Map.of()),
                    sendRegistration(gateway, LOGIN_PATH + "?attempt=query", Map.of()),
                    sendRegistration(
                            gateway,
                            LOGIN_PATH,
                            Map.of("Origin", ALLOWED_ORIGIN)
                    )
            );
            assertThat(attempts)
                    .extracting(HttpResponse::statusCode)
                    .containsExactly(200, 200, 200, 429);
            assertLoginRateLimit(attempts.getLast(), ALLOWED_ORIGIN);
        }
    }

    @Test
    void appliesIndependentQuotasToOnlyNormalizedRefreshAndLogoutPosts() throws Exception {
        try (Gateway gateway = startGateway(Policy.sessionPerIpLimited())) {
            for (String path : List.of(REFRESH_PATH, LOGOUT_PATH)) {
                assertThat(send(
                        gateway,
                        "OPTIONS",
                        path,
                        HttpRequest.BodyPublishers.noBody(),
                        Map.of(
                                "Origin", ALLOWED_ORIGIN,
                                "Access-Control-Request-Method", "POST",
                                "Access-Control-Request-Headers", "content-type,x-xsrf-token"
                        )
                ).statusCode()).isEqualTo(200);
                assertThat(send(
                        gateway,
                        "GET",
                        path,
                        HttpRequest.BodyPublishers.noBody(),
                        Map.of()
                ).statusCode()).isEqualTo(200);

                List<HttpResponse<String>> attempts = List.of(
                        sendRegistration(gateway, path, Map.of()),
                        sendRegistration(gateway, path.replace("/api/", "/api//"), Map.of()),
                        sendRegistration(gateway, path + "?attempt=query", Map.of()),
                        sendRegistration(gateway, path, Map.of("Origin", ALLOWED_ORIGIN))
                );
                assertThat(attempts)
                        .extracting(HttpResponse::statusCode)
                        .containsExactly(200, 200, 200, 429);
                if (REFRESH_PATH.equals(path)) {
                    assertRefreshRateLimit(attempts.getLast(), ALLOWED_ORIGIN);
                } else {
                    assertLogoutRateLimit(attempts.getLast(), ALLOWED_ORIGIN);
                }
            }
        }
    }

    @Test
    void appliesBodyCapToKnownAndChunkedBodies() throws Exception {
        byte[] maximumBody = new byte[8_192];
        byte[] oversizedBody = new byte[8_193];

        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> accepted = send(
                    gateway,
                    "POST",
                    REGISTRATION_PATH,
                    HttpRequest.BodyPublishers.ofByteArray(maximumBody),
                    Map.of("Content-Type", "application/octet-stream")
            );
            HttpResponse<String> knownLength = send(
                    gateway,
                    "POST",
                    REGISTRATION_PATH,
                    HttpRequest.BodyPublishers.ofByteArray(oversizedBody),
                    Map.of("Origin", ALLOWED_ORIGIN)
            );
            HttpResponse<String> chunked = send(
                    gateway,
                    "POST",
                    REGISTRATION_PATH,
                    HttpRequest.BodyPublishers.ofInputStream(
                            () -> new ByteArrayInputStream(oversizedBody)
                    ),
                    Map.of("Origin", ALLOWED_ORIGIN)
            );
            HttpResponse<String> disallowedOrigin = send(
                    gateway,
                    "POST",
                    REGISTRATION_PATH,
                    HttpRequest.BodyPublishers.ofByteArray(oversizedBody),
                    Map.of("Origin", "https://attacker.example.test")
            );

            assertThat(accepted.statusCode()).isEqualTo(200);
            assertThat(header(accepted, "X-Upstream-Id")).isIn("a", "b");
            assertPayloadTooLarge(knownLength, REGISTRATION_PATH);
            assertPayloadTooLarge(chunked, REGISTRATION_PATH);
            assertThat(disallowedOrigin.statusCode()).isEqualTo(413);
            assertThat(disallowedOrigin.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();

            byte[] maximumLoginBody = new byte[4_096];
            byte[] oversizedLoginBody = new byte[4_097];
            HttpResponse<String> acceptedLogin = send(
                    gateway,
                    "POST",
                    LOGIN_PATH,
                    HttpRequest.BodyPublishers.ofByteArray(maximumLoginBody),
                    Map.of("Content-Type", "application/octet-stream")
            );
            HttpResponse<String> oversizedLogin = send(
                    gateway,
                    "POST",
                    LOGIN_PATH,
                    HttpRequest.BodyPublishers.ofByteArray(oversizedLoginBody),
                    Map.of("Origin", ALLOWED_ORIGIN)
            );
            HttpResponse<String> chunkedLogin = send(
                    gateway,
                    "POST",
                    LOGIN_PATH,
                    HttpRequest.BodyPublishers.ofInputStream(
                            () -> new ByteArrayInputStream(oversizedLoginBody)
                    ),
                    Map.of("Origin", ALLOWED_ORIGIN)
            );

            assertThat(acceptedLogin.statusCode()).isEqualTo(200);
            assertPayloadTooLarge(oversizedLogin, LOGIN_PATH);
            assertPayloadTooLarge(chunkedLogin, LOGIN_PATH);

            byte[] maximumSessionBody = new byte[1_024];
            byte[] oversizedSessionBody = new byte[1_025];
            for (String path : List.of(REFRESH_PATH, LOGOUT_PATH)) {
                HttpResponse<String> acceptedSessionRequest = send(
                        gateway,
                        "POST",
                        path,
                        HttpRequest.BodyPublishers.ofByteArray(maximumSessionBody),
                        Map.of("Content-Type", "application/octet-stream")
                );
                HttpResponse<String> oversizedSessionRequest = send(
                        gateway,
                        "POST",
                        path,
                        HttpRequest.BodyPublishers.ofByteArray(oversizedSessionBody),
                        Map.of("Origin", ALLOWED_ORIGIN)
                );
                HttpResponse<String> chunkedSessionRequest = send(
                        gateway,
                        "POST",
                        path,
                        HttpRequest.BodyPublishers.ofInputStream(
                                () -> new ByteArrayInputStream(oversizedSessionBody)
                        ),
                        Map.of("Origin", ALLOWED_ORIGIN)
                );

                assertThat(acceptedSessionRequest.statusCode()).isEqualTo(200);
                assertPayloadTooLarge(oversizedSessionRequest, path);
                assertPayloadTooLarge(chunkedSessionRequest, path);
            }
        }
    }

    @Test
    void preservesApplicationGeneratedRateLimitResponses() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            for (String path : List.of(
                    REGISTRATION_PATH,
                    LOGIN_PATH,
                    REFRESH_PATH,
                    LOGOUT_PATH
            )) {
                HttpResponse<String> response = sendRegistration(
                        gateway,
                        path,
                        Map.of("X-Test-Upstream-Status", "429")
                );

                assertThat(response.statusCode()).isEqualTo(429);
                assertThat(response.body())
                        .contains("AUTHENTICATION_BUSY")
                        .doesNotContain(
                                "REGISTRATION_RATE_LIMITED",
                                "LOGIN_RATE_LIMITED",
                                "REFRESH_RATE_LIMITED",
                                "LOGOUT_RATE_LIMITED"
                        );
                assertThat(header(response, "X-Upstream-Error")).isEqualTo("preserved");
                assertThat(header(response, "Access-Control-Expose-Headers"))
                        .containsIgnoringCase("Retry-After");
                assertThat(response.headers().firstValue("Retry-After")).isEmpty();
            }
        }
    }

    @Test
    void overwritesUntrustedForwardingHeaders() throws Exception {
        try (Gateway gateway = startGateway(Policy.highCapacity())) {
            HttpResponse<String> response = sendRegistration(
                    gateway,
                    REGISTRATION_PATH,
                    Map.of(
                            "Forwarded", "for=spoofed.example",
                            "X-Forwarded-For", "203.0.113.10",
                            "X-Real-IP", "198.51.100.20"
                    )
            );

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(header(response, "X-Seen-Xff"))
                    .isEqualTo(header(response, "X-Seen-X-Real-Ip"))
                    .isNotBlank()
                    .matches("[0-9a-fA-F:.]+")
                    .doesNotContain("203.0.113.10", "198.51.100.20", "spoofed");
            assertThat(response.headers().firstValue("X-Seen-Forwarded")).isEmpty();
            assertThat(header(response, "X-Seen-Host")).isEqualTo(API_HOST);
            assertThat(header(response, "X-Seen-X-Forwarded-Proto")).isEqualTo("http");
        }
    }

    @Test
    void enforcesOneGlobalQuotaAcrossDistinctClientAddresses() throws Exception {
        try (Gateway gateway = startGateway(Policy.globallyLimited())) {
            Container.ExecResult firstClient = registrationFrom(UPSTREAM_A, gateway);
            Container.ExecResult secondClient = registrationFrom(UPSTREAM_B, gateway);
            Container.ExecResult thirdAttempt = registrationFrom(UPSTREAM_A, gateway);

            assertThat(firstClient.getExitCode()).isZero();
            assertThat(secondClient.getExitCode()).isZero();
            assertThat(thirdAttempt.getExitCode()).isNotZero();
            assertThat(thirdAttempt.getStdout() + thirdAttempt.getStderr())
                    .contains("429");
        }
    }

    @Test
    void keepsPerIpQuotaIndependentAcrossClientAddresses() throws Exception {
        try (Gateway gateway = startGateway(Policy.perIpLimited())) {
            assertThat(registrationFrom(UPSTREAM_A, gateway).getExitCode()).isZero();
            Container.ExecResult exhaustedClient = firstRejectedAttempt(UPSTREAM_A, gateway);
            assertThat(exhaustedClient.getExitCode()).isNotZero();
            assertThat(exhaustedClient.getStdout() + exhaustedClient.getStderr())
                    .contains("429");
            assertThat(registrationFrom(UPSTREAM_B, gateway).getExitCode()).isZero();
        }
    }

    @Test
    void enforcesOneLoginGlobalQuotaAcrossDistinctClientAddresses() throws Exception {
        try (Gateway gateway = startGateway(Policy.loginGloballyLimited())) {
            Container.ExecResult firstClient = requestFrom(UPSTREAM_A, gateway, LOGIN_PATH);
            Container.ExecResult secondClient = requestFrom(UPSTREAM_B, gateway, LOGIN_PATH);
            Container.ExecResult thirdAttempt = requestFrom(UPSTREAM_A, gateway, LOGIN_PATH);

            assertThat(firstClient.getExitCode()).isZero();
            assertThat(secondClient.getExitCode()).isZero();
            assertThat(thirdAttempt.getExitCode()).isNotZero();
            assertThat(thirdAttempt.getStdout() + thirdAttempt.getStderr())
                    .contains("429");
        }
    }

    @Test
    void enforcesIndependentRefreshAndLogoutGlobalQuotasAcrossClientAddresses() throws Exception {
        try (Gateway gateway = startGateway(Policy.sessionGloballyLimited())) {
            for (String path : List.of(REFRESH_PATH, LOGOUT_PATH)) {
                Container.ExecResult firstClient = requestFrom(UPSTREAM_A, gateway, path);
                Container.ExecResult secondClient = requestFrom(UPSTREAM_B, gateway, path);
                Container.ExecResult thirdAttempt = requestFrom(UPSTREAM_A, gateway, path);

                assertThat(firstClient.getExitCode()).isZero();
                assertThat(secondClient.getExitCode()).isZero();
                assertThat(thirdAttempt.getExitCode()).isNotZero();
                assertThat(thirdAttempt.getStdout() + thirdAttempt.getStderr())
                        .contains("429");
            }
        }
    }

    private static GenericContainer<?> upstream(String id) {
        String config = """
                server {
                    listen 8080;
                    server_name _;
                    default_type application/json;

                    add_header X-Upstream-Id "%s" always;
                    add_header X-Upstream-Error "preserved" always;
                    add_header X-Seen-Xff "$http_x_forwarded_for" always;
                    add_header X-Seen-X-Real-Ip "$http_x_real_ip" always;
                    add_header X-Seen-Forwarded "$http_forwarded" always;
                    add_header X-Seen-Host "$http_host" always;
                    add_header X-Seen-X-Forwarded-Proto "$http_x_forwarded_proto" always;

                    location / {
                        if ($http_x_test_upstream_status = "429") {
                            return 429 '{"status":429,"code":"AUTHENTICATION_BUSY"}';
                        }
                        return 200 '{"upstream":"%s"}';
                    }
                }
                """.formatted(id, id);

        return new GenericContainer<>(NGINX_IMAGE)
                .withNetwork(NETWORK)
                .withNetworkAliases("backend")
                .withExposedPorts(8080)
                .withCopyToContainer(
                        Transferable.of(config.getBytes(StandardCharsets.UTF_8), 0644),
                        "/etc/nginx/conf.d/default.conf"
                )
                .waitingFor(Wait.forHttp("/").forPort(8080).forStatusCode(200)
                        .withStartupTimeout(Duration.ofSeconds(30)));
    }

    private static Gateway startGateway(Policy policy) {
        String networkAlias = "gateway-" + UUID.randomUUID();
        GenericContainer<?> container = new GenericContainer<>(NGINX_IMAGE)
                .withNetwork(NETWORK)
                .withNetworkAliases(networkAlias)
                .withExposedPorts(8080)
                .withCopyFileToContainer(
                        MountableFile.forHostPath(CONFIG_TEMPLATE),
                        "/etc/nginx/templates/default.conf.template"
                )
                .withEnv(
                        "NGINX_ENVSUBST_FILTER",
                        "^(API_|AUTH_|BACKEND_|BUSINESS_|LOGIN_|LOGOUT_|REFRESH_|REGISTRATION_|UPLOAD_)"
                )
                .withEnv("API_SERVER_NAME", API_HOST)
                .withEnv("BACKEND_HOST", "backend")
                .withEnv("BACKEND_PORT", "8080")
                .withEnv("BACKEND_DNS_RESOLVER", "127.0.0.11")
                .withEnv("AUTH_MAX_REQUEST_BODY_BYTES", "8192")
                .withEnv("BUSINESS_MAX_REQUEST_BODY_BYTES", "8192")
                .withEnv("UPLOAD_MAX_REQUEST_BODY_BYTES", Integer.toString(UPLOAD_MAX_REQUEST_BODY_BYTES))
                .withEnv("REGISTRATION_CORS_ALLOWED_ORIGIN", ALLOWED_ORIGIN)
                .withEnv("REGISTRATION_PER_IP_RATE", policy.perIpRate())
                .withEnv("REGISTRATION_PER_IP_BURST", policy.perIpBurst())
                .withEnv("REGISTRATION_GLOBAL_RATE", policy.globalRate())
                .withEnv("REGISTRATION_GLOBAL_BURST", policy.globalBurst())
                .withEnv("REGISTRATION_RETRY_AFTER_SECONDS", "60")
                .withEnv("LOGIN_MAX_REQUEST_BODY_BYTES", "4096")
                .withEnv("LOGIN_PER_IP_RATE", policy.loginPerIpRate())
                .withEnv("LOGIN_PER_IP_BURST", policy.loginPerIpBurst())
                .withEnv("LOGIN_GLOBAL_RATE", policy.loginGlobalRate())
                .withEnv("LOGIN_GLOBAL_BURST", policy.loginGlobalBurst())
                .withEnv("LOGIN_RETRY_AFTER_SECONDS", "60")
                .withEnv("REFRESH_MAX_REQUEST_BODY_BYTES", "1024")
                .withEnv("REFRESH_PER_IP_RATE", policy.refreshPerIpRate())
                .withEnv("REFRESH_PER_IP_BURST", policy.refreshPerIpBurst())
                .withEnv("REFRESH_GLOBAL_RATE", policy.refreshGlobalRate())
                .withEnv("REFRESH_GLOBAL_BURST", policy.refreshGlobalBurst())
                .withEnv("REFRESH_RETRY_AFTER_SECONDS", "60")
                .withEnv("LOGOUT_MAX_REQUEST_BODY_BYTES", "1024")
                .withEnv("LOGOUT_PER_IP_RATE", policy.logoutPerIpRate())
                .withEnv("LOGOUT_PER_IP_BURST", policy.logoutPerIpBurst())
                .withEnv("LOGOUT_GLOBAL_RATE", policy.logoutGlobalRate())
                .withEnv("LOGOUT_GLOBAL_BURST", policy.logoutGlobalBurst())
                .withEnv("LOGOUT_RETRY_AFTER_SECONDS", "60")
                .waitingFor(Wait.forListeningPort()
                        .withStartupTimeout(Duration.ofSeconds(30)));
        container.start();
        return new Gateway(container, networkAlias);
    }

    private static List<HttpResponse<String>> sendConcurrentRegistrations(
            Gateway gateway,
            int requestCount
    ) throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(requestCount);
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        List<Future<HttpResponse<String>>> futures = new ArrayList<>();

        try {
            for (int attempt = 0; attempt < requestCount; attempt++) {
                int requestNumber = attempt;
                futures.add(executor.submit(() -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return sendRegistration(
                            gateway,
                            REGISTRATION_PATH + "?attempt=" + requestNumber,
                            Map.of(
                                    "Origin", ALLOWED_ORIGIN,
                                    "X-Forwarded-For", "spoofed-" + requestNumber
                            )
                    );
                }));
            }

            List<HttpResponse<String>> responses = new ArrayList<>();
            for (Future<HttpResponse<String>> future : futures) {
                responses.add(future.get(20, TimeUnit.SECONDS));
            }
            return responses;
        } finally {
            executor.shutdownNow();
        }
    }

    private static HttpResponse<String> sendRegistration(
            Gateway gateway,
            String path,
            Map<String, String> headers
    ) throws Exception {
        return send(
                gateway,
                "POST",
                path,
                HttpRequest.BodyPublishers.ofString("{}"),
                headers
        );
    }

    private static HttpResponse<String> send(
            Gateway gateway,
            String method,
            String path,
            HttpRequest.BodyPublisher body,
            Map<String, String> headers
    ) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(gateway.uri(path))
                .timeout(Duration.ofSeconds(10));
        headers.forEach(request::header);

        return HTTP_CLIENT.send(
                request.method(method, body).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
    }

    private static Container.ExecResult firstRejectedAttempt(
            GenericContainer<?> client,
            Gateway gateway
    ) throws Exception {
        for (int attempt = 0; attempt < 4; attempt++) {
            Container.ExecResult result = registrationFrom(client, gateway);
            if (result.getExitCode() != 0) {
                return result;
            }
        }
        throw new AssertionError("client quota was not exhausted");
    }

    private static Container.ExecResult registrationFrom(
            GenericContainer<?> client,
            Gateway gateway
    )
            throws Exception {
        return requestFrom(client, gateway, REGISTRATION_PATH);
    }

    private static Container.ExecResult requestFrom(
            GenericContainer<?> client,
            Gateway gateway,
            String path
    ) throws Exception {
        return client.execInContainer(
                "sh",
                "-c",
                "wget -S -O /dev/null --header='Host: " + API_HOST
                        + "' --header='Content-Type: application/json' --post-data='{}' "
                        + "http://" + gateway.networkAlias() + ":8080"
                        + path + " 2>&1"
        );
    }

    private static void assertRegistrationRateLimit(
            HttpResponse<String> response,
            String allowedOrigin
    ) {
        assertAuthRateLimit(
                response,
                allowedOrigin,
                "REGISTRATION_RATE_LIMITED",
                "Too many registration attempts",
                REGISTRATION_PATH
        );
    }

    private static void assertLoginRateLimit(
            HttpResponse<String> response,
            String allowedOrigin
    ) {
        assertAuthRateLimit(
                response,
                allowedOrigin,
                "LOGIN_RATE_LIMITED",
                "Too many login attempts",
                LOGIN_PATH
        );
    }

    private static void assertRefreshRateLimit(
            HttpResponse<String> response,
            String allowedOrigin
    ) {
        assertAuthRateLimit(
                response,
                allowedOrigin,
                "REFRESH_RATE_LIMITED",
                "Too many refresh attempts",
                REFRESH_PATH
        );
    }

    private static void assertLogoutRateLimit(
            HttpResponse<String> response,
            String allowedOrigin
    ) {
        assertAuthRateLimit(
                response,
                allowedOrigin,
                "LOGOUT_RATE_LIMITED",
                "Too many logout attempts",
                LOGOUT_PATH
        );
    }

    private static void assertAuthRateLimit(
            HttpResponse<String> response,
            String allowedOrigin,
            String expectedCode,
            String expectedMessage,
            String expectedPath
    ) {
        assertThat(response.statusCode()).isEqualTo(429);
        assertThat(header(response, "Content-Type")).startsWith("application/problem+json");
        assertThat(header(response, "Cache-Control")).isEqualTo("no-store");
        assertThat(header(response, "Retry-After")).isEqualTo("60");
        assertThat(header(response, "Access-Control-Expose-Headers"))
                .containsIgnoringCase("Retry-After")
                .containsIgnoringCase("X-XSRF-TOKEN");
        assertThat(header(response, "Access-Control-Allow-Origin")).isEqualTo(allowedOrigin);
        assertThat(header(response, "Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(header(response, "Vary")).contains("Origin");
        JsonNode problem = parseProblem(response);
        assertThat(problem.path("status").asInt()).isEqualTo(429);
        assertThat(problem.path("code").asText()).isEqualTo(expectedCode);
        assertThat(problem.path("message").asText()).isEqualTo(expectedMessage);
        assertThat(problem.path("path").asText()).isEqualTo(expectedPath);
        assertThat(problem.path("fieldErrors").isObject()).isTrue();
        assertThat(problem.path("fieldErrors").isEmpty()).isTrue();
        assertThat(OffsetDateTime.parse(problem.path("timestamp").asText())).isNotNull();
    }

    private static void assertPayloadTooLarge(
            HttpResponse<String> response,
            String expectedPath
    ) {
        assertThat(response.statusCode()).isEqualTo(413);
        assertThat(header(response, "Content-Type")).startsWith("application/problem+json");
        assertThat(header(response, "Cache-Control")).isEqualTo("no-store");
        assertThat(header(response, "Access-Control-Allow-Origin")).isEqualTo(ALLOWED_ORIGIN);
        assertThat(header(response, "Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(response.headers().firstValue("Retry-After")).isEmpty();
        assertThat(response.headers().firstValue("X-Upstream-Id")).isEmpty();
        JsonNode problem = parseProblem(response);
        assertThat(problem.path("status").asInt()).isEqualTo(413);
        assertThat(problem.path("code").asText()).isEqualTo("PAYLOAD_TOO_LARGE");
        assertThat(problem.path("message").asText())
                .isEqualTo("Request body exceeds the allowed size");
        assertThat(problem.path("path").asText()).isEqualTo(expectedPath);
        assertThat(problem.path("fieldErrors").isObject()).isTrue();
        assertThat(problem.path("fieldErrors").isEmpty()).isTrue();
        assertThat(OffsetDateTime.parse(problem.path("timestamp").asText())).isNotNull();
    }

    private static JsonNode parseProblem(HttpResponse<String> response) {
        try {
            return OBJECT_MAPPER.readTree(response.body());
        } catch (Exception exception) {
            throw new AssertionError("Nginx did not return valid problem JSON", exception);
        }
    }

    private static String header(HttpResponse<String> response, String name) {
        return response.headers().firstValue(name).orElse("");
    }

    private record Gateway(
            GenericContainer<?> container,
            String networkAlias
    ) implements AutoCloseable {

        URI uri(String path) {
            String host = "localhost".equals(container.getHost())
                    ? "127.0.0.1"
                    : container.getHost();
            return URI.create(
                    "http://" + host + ":"
                            + container.getMappedPort(8080) + path
            );
        }

        @Override
        public void close() {
            container.stop();
        }
    }

    private record Policy(
            String perIpRate,
            String perIpBurst,
            String globalRate,
            String globalBurst,
            String loginPerIpRate,
            String loginPerIpBurst,
            String loginGlobalRate,
            String loginGlobalBurst,
            String refreshPerIpRate,
            String refreshPerIpBurst,
            String refreshGlobalRate,
            String refreshGlobalBurst,
            String logoutPerIpRate,
            String logoutPerIpBurst,
            String logoutGlobalRate,
            String logoutGlobalBurst
    ) {

        static Policy highCapacity() {
            return new Policy(
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100"
            );
        }

        static Policy productionDefaults() {
            return new Policy(
                    "5r/m", "2", "30r/m", "10",
                    "10r/m", "5", "120r/m", "30",
                    "30r/m", "10", "300r/m", "100",
                    "30r/m", "10", "300r/m", "100"
            );
        }

        static Policy perIpLimited() {
            return new Policy(
                    "1r/m", "2", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100"
            );
        }

        static Policy globallyLimited() {
            return new Policy(
                    "100r/s", "100", "1r/m", "1",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100"
            );
        }

        static Policy loginPerIpLimited() {
            return new Policy(
                    "100r/s", "100", "100r/s", "100",
                    "1r/m", "2", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100"
            );
        }

        static Policy loginGloballyLimited() {
            return new Policy(
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "1r/m", "1",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100"
            );
        }

        static Policy sessionPerIpLimited() {
            return new Policy(
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100",
                    "1r/m", "2", "100r/s", "100",
                    "1r/m", "2", "100r/s", "100"
            );
        }

        static Policy sessionGloballyLimited() {
            return new Policy(
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "100r/s", "100",
                    "100r/s", "100", "1r/m", "1",
                    "100r/s", "100", "1r/m", "1"
            );
        }
    }
}
