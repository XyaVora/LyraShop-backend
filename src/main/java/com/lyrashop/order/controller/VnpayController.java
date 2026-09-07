package com.lyrashop.order.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.order.dto.VnpayIpnResponse;
import com.lyrashop.order.service.OrderService;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
public class VnpayController {

    private final OrderService orders;

    public VnpayController(OrderService orders) {
        this.orders = orders;
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
