package com.lyrashop.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateOrderRequest(
        @NotBlank @Size(max = 2000) String shippingAddress,
        @NotBlank @Size(max = 20) String shippingPhone,
        @Size(max = 2000) String note,
        @NotBlank String paymentMethod,
        @Size(max = 30) String voucherCode,
        boolean giftWrap,
        @Size(max = 500) String giftMessage,
        @PositiveOrZero long loyaltyCoins
) {
    public CreateOrderRequest {
        shippingAddress = shippingAddress == null ? null : shippingAddress.strip();
        shippingPhone = shippingPhone == null ? null : shippingPhone.strip();
        note = note == null || note.isBlank() ? null : note.strip();
        paymentMethod = paymentMethod == null ? null : paymentMethod.strip();
        voucherCode = voucherCode == null || voucherCode.isBlank() ? null : voucherCode.strip().toUpperCase();
        giftMessage = giftMessage == null || giftMessage.isBlank() ? null : giftMessage.strip();
    }
}
