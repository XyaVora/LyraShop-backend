package com.lyrashop.user.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.user.dto.ProfileResponse;
import com.lyrashop.user.dto.UpdateProfileRequest;
import com.lyrashop.user.service.ProfileService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/me")
public class ProfileController {

    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse get(Authentication authentication) {
        return ProfileResponse.from(profiles.get(userId(authentication)));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse update(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ProfileResponse.from(profiles.update(userId(authentication), request));
    }

    private static UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
