package com.lyrashop.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.lyrashop.auth.controller.AuthController;
import com.lyrashop.auth.dto.RegisterRequest;
import com.lyrashop.auth.service.LoginService;
import com.lyrashop.auth.service.RegistrationService;

class GlobalExceptionHandlerTests {

    private final RegistrationService registrationService = mock(RegistrationService.class);
    private final LoginService loginService = mock(LoginService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(registrationService, loginService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void returnsRetryGuidanceWhenAuthenticationCapacityIsExhausted() throws Exception {
        String rejectedPassword = "valid-password-material";
        when(registrationService.register(any(RegisterRequest.class)))
                .thenThrow(new AuthenticationCapacityExceededException(3));

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "capacity@example.com",
                                  "password": "%s",
                                  "fullName": "Capacity Test",
                                  "phone": null
                                }
                                """.formatted(rejectedPassword)))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "3"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_BUSY"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain(rejectedPassword, "capacity@example.com");
    }
}
