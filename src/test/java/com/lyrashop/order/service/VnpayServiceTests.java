package com.lyrashop.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lyrashop.config.VnpayProperties;

class VnpayServiceTests {

    private final VnpayService vnpay = new VnpayService(new VnpayProperties(
            "TMNCODE",
            "secret",
            "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
            "https://shop.example.test/vnpay-return",
            null
    ));

    @Test
    void verifiesHmacSignatures() {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Amount", "1250");
        params.put("vnp_TxnRef", "550e8400e29b41d4a716446655440000");
        String hashData = "vnp_Amount=1250&vnp_TxnRef=550e8400e29b41d4a716446655440000";
        params.put("vnp_SecureHash", VnpayService.hmacSha512("secret", hashData));
        assertThat(vnpay.signatureMatches(params)).isTrue();
        params.put("vnp_SecureHash", "deadbeef");
        assertThat(vnpay.signatureMatches(params)).isFalse();
    }

    @Test
    void convertsVndAmountsAndTxnRefs() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        assertThat(VnpayService.vndAmount(new BigDecimal("12.50"))).isEqualTo("1250");
        assertThat(VnpayService.txnRef(id)).isEqualTo("550e8400e29b41d4a716446655440000");
        assertThat(VnpayService.orderId("550e8400e29b41d4a716446655440000")).isEqualTo(id);
        assertThat(vnpay.enabled()).isTrue();
        assertThat(new VnpayService(new VnpayProperties(null, null, null, null, null)).enabled()).isFalse();
    }
}
