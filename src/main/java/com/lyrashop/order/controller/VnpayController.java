package com.lyrashop.order.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.order.dto.VnpayIpnResponse;
import com.lyrashop.order.service.OrderService;
import com.lyrashop.order.service.VnpayService;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
public class VnpayController {

    private final OrderService orders;
    private final VnpayService vnpay;

    public VnpayController(OrderService orders, VnpayService vnpay) {
        this.orders = orders;
        this.vnpay = vnpay;
    }

    @GetMapping(path = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Boolean> status() {
        return Map.of("enabled", vnpay.enabled());
    }

    @GetMapping(path = "/ipn", produces = MediaType.APPLICATION_JSON_VALUE)
    public VnpayIpnResponse ipn(@RequestParam Map<String, String> params) {
        return orders.confirmVnpay(params);
    }

    @GetMapping(path = "/return", produces = MediaType.APPLICATION_JSON_VALUE)
    public VnpayIpnResponse returned(@RequestParam Map<String, String> params) {
        return orders.confirmVnpay(params);
    }
}
