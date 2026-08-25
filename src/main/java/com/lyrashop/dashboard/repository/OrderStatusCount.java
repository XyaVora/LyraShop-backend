package com.lyrashop.dashboard.repository;

import com.lyrashop.order.entity.OrderStatus;

public interface OrderStatusCount {

    OrderStatus getStatus();

    Long getTotal();
}
