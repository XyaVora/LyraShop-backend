package com.lyrashop.promotion.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.promotion.dto.PromotionResponse;
import com.lyrashop.promotion.service.PromotionService;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {
    private final PromotionService service;
    public PromotionController(PromotionService service) { this.service = service; }

    @GetMapping(path = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PromotionResponse> active() {
        return service.active().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
