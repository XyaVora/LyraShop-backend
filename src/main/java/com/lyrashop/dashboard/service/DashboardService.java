package com.lyrashop.dashboard.service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.dashboard.dto.DashboardBestSellerResponse;
import com.lyrashop.dashboard.dto.DashboardOrderCountsResponse;
import com.lyrashop.dashboard.dto.DashboardResponse;
import com.lyrashop.dashboard.dto.DashboardRevenueResponse;
import com.lyrashop.dashboard.repository.OrderStatusCount;
import com.lyrashop.order.entity.OrderStatus;
import com.lyrashop.order.repository.ShopOrderRepository;

@Service
public class DashboardService {

    private static final int BEST_SELLER_LIMIT = 10;
    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2);

    private final ShopOrderRepository orders;

    public DashboardService(ShopOrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public DashboardResponse snapshot() {
        BigDecimal paidTotal = orders.sumPaidTotal();
        if (paidTotal == null) {
            paidTotal = ZERO_MONEY;
        }
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, 0L);
        }
        for (OrderStatusCount row : orders.countGroupedByStatus()) {
            if (row.getStatus() != null && row.getTotal() != null) {
                counts.put(row.getStatus(), row.getTotal());
            }
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        List<DashboardBestSellerResponse> bestSellers = orders
                .findBestSellers(PageRequest.of(0, BEST_SELLER_LIMIT))
                .stream()
                .map(row -> new DashboardBestSellerResponse(
                        row.getProductId(),
                        row.getProductName(),
                        row.getQuantitySold() == null ? 0L : row.getQuantitySold(),
                        row.getRevenue() == null ? ZERO_MONEY : row.getRevenue()
                ))
                .toList();
        return new DashboardResponse(
                new DashboardRevenueResponse(paidTotal, orders.countPaid()),
                new DashboardOrderCountsResponse(
                        total,
                        counts.get(OrderStatus.PENDING),
                        counts.get(OrderStatus.CONFIRMED),
                        counts.get(OrderStatus.PROCESSING),
                        counts.get(OrderStatus.SHIPPING),
                        counts.get(OrderStatus.DELIVERED),
                        counts.get(OrderStatus.CANCELLED)
                ),
                bestSellers
        );
    }
}
