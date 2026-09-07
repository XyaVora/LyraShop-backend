package com.lyrashop.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.user.dto.AdminUserResponse;
import com.lyrashop.user.dto.UpdateUserRoleRequest;
import com.lyrashop.user.dto.UpdateUserStatusRequest;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;
import com.lyrashop.user.service.UserNotFoundException;
import com.lyrashop.user.service.UserRoleService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserRepository users;
    private final UserRoleService userRoleService;

    public AdminUserController(UserRepository users, UserRoleService userRoleService) {
        this.users = users;
        this.userRoleService = userRoleService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminUserResponse> list() {
        return users.findAll().stream().map(AdminUserResponse::from).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminUserResponse updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UUID userId;
        try {
            userId = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new UserNotFoundException();
        }
        User user = users.findById(userId).orElseThrow(UserNotFoundException::new);
        if (Boolean.TRUE.equals(request.active())) {
            user.activate();
        } else {
            user.deactivate();
        }
        return AdminUserResponse.from(users.saveAndFlush(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}/role", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminUserResponse updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        UUID userId;
        try {
            userId = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new UserNotFoundException();
        }
        return AdminUserResponse.from(userRoleService.assignRole(userId, request.role()));
    }
}
