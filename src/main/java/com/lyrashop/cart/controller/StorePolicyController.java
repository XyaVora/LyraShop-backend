package com.lyrashop.cart.controller;

import java.math.BigDecimal;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.cart.service.StorePricing;

@RestController
@RequestMapping("/api/v1/store-policy")
public class StorePolicyController {
    public record StorePolicyResponse(
            BigDecimal freeShippingThreshold,
            BigDecimal standardShippingFee,
            BigDecimal giftWrapFee,
            int returnWindowDays
    ) {}

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public StorePolicyResponse get() {
        return new StorePolicyResponse(StorePricing.freeShippingThreshold(), StorePricing.standardShippingFee(),
                StorePricing.giftWrapFee(), 30);
    }
}
