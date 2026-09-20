package com.lyrashop.order.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.cart.service.CartService;
import com.lyrashop.order.dto.VoucherQuoteResponse;
import com.lyrashop.order.service.VoucherService;

@RestController
@RequestMapping("/api/v1/vouchers")
public class VoucherController {
    private final CartService carts;
    private final VoucherService vouchers;

    public VoucherController(CartService carts, VoucherService vouchers) {
        this.carts = carts;
        this.vouchers = vouchers;
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VoucherQuoteResponse> available(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return vouchers.available(userId, merchandiseTotal(userId));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping(path = "/quote", produces = MediaType.APPLICATION_JSON_VALUE)
    public VoucherQuoteResponse quote(Authentication authentication, @RequestParam String code) {
        UUID userId = UUID.fromString(authentication.getName());
        return vouchers.quote(userId, code, merchandiseTotal(userId));
    }

    private BigDecimal merchandiseTotal(UUID userId) {
        var cart = carts.get(userId);
        return cart.subtotalAmount().subtract(cart.discountAmount());
    }
}
