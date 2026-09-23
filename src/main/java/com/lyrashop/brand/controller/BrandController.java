package com.lyrashop.brand.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.brand.dto.BrandResponse;
import com.lyrashop.brand.service.BrandService;

@RestController @RequestMapping("/api/v1/brand")
public class BrandController {
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public BrandResponse get() {
        return brandService.get();
    }
}
