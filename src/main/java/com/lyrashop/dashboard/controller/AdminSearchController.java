package com.lyrashop.dashboard.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.dashboard.dto.AdminSearchResultResponse;
import com.lyrashop.dashboard.service.AdminSearchService;

@RestController
@RequestMapping("/api/v1/admin/search")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSearchController {
    private final AdminSearchService searchService;

    public AdminSearchController(AdminSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminSearchResultResponse> search(@RequestParam(name = "q", defaultValue = "") String query) {
        return searchService.search(query);
    }
}
