package com.lyrashop.brand.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.brand.dto.BrandResponse;
import com.lyrashop.brand.entity.BrandSettings;
import com.lyrashop.brand.repository.BrandSettingsRepository;

@Service
public class BrandService {
    private static final short PRIMARY_BRAND_ID = 1;

    private final BrandSettingsRepository brands;

    public BrandService(BrandSettingsRepository brands) {
        this.brands = brands;
    }

    @Transactional(readOnly = true)
    public BrandResponse get() {
        BrandSettings brand = brands.findById(PRIMARY_BRAND_ID)
                .orElseThrow(() -> new IllegalStateException("Brand settings are not configured"));
        return new BrandResponse(brand.getName(), brand.getTagline(), brand.getStory(), brand.getFounded(),
                brand.getHotline(), brand.getEmail(), brand.getAddress());
    }
}
