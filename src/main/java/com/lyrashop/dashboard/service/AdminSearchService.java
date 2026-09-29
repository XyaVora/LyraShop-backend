package com.lyrashop.dashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.dashboard.dto.AdminSearchResultResponse;
import com.lyrashop.order.entity.OrderStatus;
import com.lyrashop.order.repository.ShopOrderRepository;
import com.lyrashop.user.repository.UserRepository;

@Service
public class AdminSearchService {
    private static final int PER_TYPE_LIMIT = 4;
    private static final int TOTAL_LIMIT = 8;

    private final ProductRepository products;
    private final ShopOrderRepository orders;
    private final UserRepository users;

    public AdminSearchService(ProductRepository products, ShopOrderRepository orders,
            UserRepository users) {
        this.products = products;
        this.orders = orders;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<AdminSearchResultResponse> search(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.strip();
        if (query.length() < 2) return List.of();
        PageRequest firstFour = PageRequest.of(0, PER_TYPE_LIMIT);
        List<AdminSearchResultResponse> result = new ArrayList<>();

        products.searchForAdmin(query, firstFour).forEach(product -> result.add(
                new AdminSearchResultResponse("PRODUCT", product.getId(), product.getName(),
                        product.getSlug(), "/products/" + product.getId())));

        UUID orderId = parseUuid(query);
        if (orderId != null) orders.findById(orderId).ifPresent(order -> result.add(orderResult(order)));
        orders.searchForAdmin(query, firstFour).stream()
                .filter(order -> orderId == null || !order.getId().equals(orderId))
                .limit(PER_TYPE_LIMIT)
                .forEach(order -> result.add(orderResult(order)));

        users.searchForAdmin(query, firstFour).forEach(user -> result.add(
                new AdminSearchResultResponse("USER", user.getId(), user.getEmail(),
                        user.getRole().name(), "/users")));

        return result.stream().limit(TOTAL_LIMIT).toList();
    }

    private static AdminSearchResultResponse orderResult(com.lyrashop.order.entity.ShopOrder order) {
        String status = order.getStatus() == null ? OrderStatus.PENDING.name() : order.getStatus().name();
        String phone = order.getShippingPhone() == null ? "" : order.getShippingPhone();
        return new AdminSearchResultResponse("ORDER", order.getId(),
                "#" + order.getId().toString().substring(0, 8).toUpperCase(Locale.ROOT),
                phone + " · " + status, "/orders/" + order.getId());
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
