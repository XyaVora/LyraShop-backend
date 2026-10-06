package com.lyrashop.dashboard.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.lyrashop.dashboard.dto.AdminSearchResultResponse;
import com.lyrashop.dashboard.service.AdminSearchService;

@RestController
@RequestMapping("/api/v1/admin/search")
@PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER','ORDER_MANAGER','SUPPORT')")
public class AdminSearchController {
    private final AdminSearchService searchService;

    public AdminSearchController(AdminSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminSearchResultResponse> search(Authentication authentication,
            @RequestParam(name = "q", defaultValue = "") String query) {
        String role = authentication.getAuthorities().stream().findFirst()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .orElse("");
        return searchService.search(query, role);
    }
}
