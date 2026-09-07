package com.lyrashop.order.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.lyrashop.config.VnpayProperties;
import com.lyrashop.order.entity.ShopOrder;

@Service
public class VnpayService {

    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayProperties properties;

    public VnpayService(VnpayProperties properties) {
        this.properties = properties;
    }

    public boolean enabled() {
        return properties.enabled();
    }

    public String paymentUrl(ShopOrder order, String clientIp) {
        if (!enabled()) {
            throw new InvalidPaymentMethodException();
        }
        Map<String, String> fields = new TreeMap<>();
        fields.put("vnp_Version", "2.1.0");
        fields.put("vnp_Command", "pay");
        fields.put("vnp_TmnCode", properties.tmnCode());
        fields.put("vnp_Amount", vndAmount(order.getTotalAmount()));
        fields.put("vnp_CurrCode", "VND");
        fields.put("vnp_TxnRef", txnRef(order.getId()));
        fields.put("vnp_OrderInfo", "LyraShop order " + order.getId());
        fields.put("vnp_OrderType", "other");
        fields.put("vnp_Locale", "vn");
        fields.put("vnp_ReturnUrl", properties.returnUrl());
        fields.put("vnp_IpAddr", clientIp == null || clientIp.isBlank() ? "127.0.0.1" : clientIp);
        fields.put("vnp_CreateDate", ZonedDateTime.now(VIETNAM).format(TIMESTAMP));
        if (properties.ipnUrl() != null) {
            fields.put("vnp_IpnUrl", properties.ipnUrl());
        }
        String hashData = query(fields, false);
        String query = query(fields, true);
        return properties.payUrl() + "?" + query + "&vnp_SecureHash=" + hmacSha512(properties.hashSecret(), hashData);
    }

    public boolean signatureMatches(Map<String, String> params) {
        if (!enabled()) {
            return false;
        }
        String provided = params.get("vnp_SecureHash");
        if (provided == null || provided.isBlank()) {
            return false;
        }
        Map<String, String> fields = new TreeMap<>();
        params.forEach((key, value) -> {
            if (key != null && key.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)
                    && value != null && !value.isBlank()) {
                fields.put(key, value);
            }
        });
        return hmacSha512(properties.hashSecret(), query(fields, false)).equalsIgnoreCase(provided);
    }

    public static String txnRef(UUID orderId) {
        return orderId.toString().replace("-", "");
    }

    public static UUID orderId(String txnRef) {
        if (txnRef == null || txnRef.length() != 32) {
            throw new OrderNotFoundException();
        }
        try {
            return UUID.fromString(
                    txnRef.substring(0, 8) + "-"
                            + txnRef.substring(8, 12) + "-"
                            + txnRef.substring(12, 16) + "-"
                            + txnRef.substring(16, 20) + "-"
                            + txnRef.substring(20)
            );
        } catch (IllegalArgumentException exception) {
            throw new OrderNotFoundException();
        }
    }

    public static String vndAmount(BigDecimal total) {
        return total.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).toPlainString();
    }

    public static String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign VNPay payload", exception);
        }
    }

    private static String query(Map<String, String> fields, boolean encodeKeys) {
        StringBuilder builder = new StringBuilder();
        fields.forEach((key, value) -> {
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            if (encodeKeys) {
                builder.append(encode(key));
            } else {
                builder.append(key);
            }
            builder.append('=');
            builder.append(encode(value));
        });
        return builder.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }
}
