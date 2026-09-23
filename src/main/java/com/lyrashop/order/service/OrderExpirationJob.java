package com.lyrashop.order.service;

import java.time.Instant;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lyrashop.order.repository.ShopOrderRepository;

@Component
public class OrderExpirationJob {
    private static final int BATCH_SIZE = 100;

    private final ShopOrderRepository orders;
    private final OrderService orderService;

    public OrderExpirationJob(ShopOrderRepository orders, OrderService orderService) {
        this.orders = orders;
        this.orderService = orderService;
    }

    @Scheduled(
            fixedDelayString = "${app.order.expiration-sweep-delay:1m}",
            initialDelayString = "${app.order.expiration-sweep-initial-delay:30s}"
    )
    public void expirePendingOrders() {
        while (true) {
            var ids = orders.findExpiredPendingIds(Instant.now(), PageRequest.of(0, BATCH_SIZE));
            if (ids.isEmpty()) return;
            ids.forEach(id -> orderService.expirePending(id, Instant.now()));
            if (ids.size() < BATCH_SIZE) return;
        }
    }
}
