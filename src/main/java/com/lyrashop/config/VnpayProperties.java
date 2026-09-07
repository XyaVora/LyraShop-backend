package com.lyrashop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.vnpay")
public record VnpayProperties(
        String tmnCode,
        String hashSecret,
        String payUrl,
        String returnUrl,
        String ipnUrl
) {

    public VnpayProperties {
        tmnCode = blankToNull(tmnCode);
        hashSecret = blankToNull(hashSecret);
        payUrl = blankToNull(payUrl);
        returnUrl = blankToNull(returnUrl);
        ipnUrl = blankToNull(ipnUrl);
    }

    public boolean enabled() {
        return tmnCode != null && hashSecret != null && payUrl != null && returnUrl != null;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}
