package com.lyrashop.search.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.search.dto.SearchHistoryRequest;
import com.lyrashop.search.dto.SearchHistoryResponse;
import com.lyrashop.search.service.SearchHistoryService;

import jakarta.validation.Valid;

@Validated @RestController @RequestMapping("/api/v1/search-history")
public class SearchHistoryController {
    private final SearchHistoryService service;
    public SearchHistoryController(SearchHistoryService service) { this.service = service; }
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SearchHistoryResponse> list(Authentication auth) { return service.list(userId(auth)); }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SearchHistoryResponse> add(Authentication auth, @Valid @RequestBody SearchHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(userId(auth), request.query()));
    }
    @DeleteMapping public ResponseEntity<Void> clear(Authentication auth) {
        service.clear(userId(auth)); return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/item") public ResponseEntity<Void> remove(Authentication auth, @RequestParam String query) {
        service.remove(userId(auth), query); return ResponseEntity.noContent().build();
    }
    private static UUID userId(Authentication auth) { return UUID.fromString(auth.getName()); }
}
