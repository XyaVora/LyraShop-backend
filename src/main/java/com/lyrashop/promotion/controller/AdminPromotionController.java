package com.lyrashop.promotion.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.promotion.dto.PromotionRequest;
import com.lyrashop.promotion.dto.PromotionResponse;
import com.lyrashop.promotion.service.PromotionService;

import jakarta.validation.Valid;

@RestController @RequestMapping("/api/v1/admin/promotions") @PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionController {
    private final PromotionService service;
    public AdminPromotionController(PromotionService service) { this.service = service; }
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE) public List<PromotionResponse> list() { return service.list(); }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PromotionResponse update(@PathVariable UUID id, @Valid @RequestBody PromotionRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
