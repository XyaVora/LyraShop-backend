package com.lyrashop.dashboard.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lyrashop.dashboard.dto.AdminNotificationResponse;
import com.lyrashop.dashboard.service.AdminNotificationService;

@RestController
@RequestMapping("/api/v1/admin/notifications")
public class AdminNotificationController {
    private final AdminNotificationService service;
    public AdminNotificationController(AdminNotificationService service) { this.service = service; }

    @PreAuthorize("hasAnyRole('ADMIN','ORDER_MANAGER','SUPPORT')")
    @GetMapping
    public List<AdminNotificationResponse> list(Authentication authentication) {
        return service.list(UUID.fromString(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORDER_MANAGER','SUPPORT')")
    @PutMapping("/{key}/read")
    public ResponseEntity<Void> read(Authentication authentication, @PathVariable String key) {
        service.markRead(UUID.fromString(authentication.getName()), key);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORDER_MANAGER','SUPPORT')")
    @PutMapping("/read-all")
    public ResponseEntity<Void> readAll(Authentication authentication) {
        service.markAllRead(UUID.fromString(authentication.getName()));
        return ResponseEntity.noContent().build();
    }
}

