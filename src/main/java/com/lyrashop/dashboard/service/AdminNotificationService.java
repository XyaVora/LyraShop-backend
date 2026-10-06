package com.lyrashop.dashboard.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lyrashop.dashboard.dto.AdminNotificationResponse;
import com.lyrashop.dashboard.entity.AdminNotificationRead;
import com.lyrashop.dashboard.repository.AdminNotificationReadRepository;
import com.lyrashop.order.entity.ShopOrder;
import com.lyrashop.order.repository.ShopOrderRepository;

@Service
public class AdminNotificationService {
    private final ShopOrderRepository orders;
    private final AdminNotificationReadRepository reads;

    public AdminNotificationService(ShopOrderRepository orders, AdminNotificationReadRepository reads) {
        this.orders = orders;
        this.reads = reads;
    }

    @Transactional(readOnly = true)
    public List<AdminNotificationResponse> list(UUID adminId) {
        List<ShopOrder> actionable = orders.findActionableForAdmin(PageRequest.of(0, 20));
        List<String> keys = actionable.stream().map(AdminNotificationService::key).toList();
        Set<String> readKeys = keys.isEmpty() ? Set.of() : new HashSet<>(reads
                .findAllByAdminIdAndNotificationKeyIn(adminId, keys).stream()
                .map(AdminNotificationRead::getNotificationKey).toList());
        return actionable.stream().map(order -> response(order, readKeys.contains(key(order)))).toList();
    }

    @Transactional
    public void markRead(UUID adminId, String key) {
        if (key == null || key.isBlank() || key.length() > 120) return;
        if (!reads.existsByAdminIdAndNotificationKey(adminId, key)) {
            try { reads.saveAndFlush(new AdminNotificationRead(adminId, key)); }
            catch (DataIntegrityViolationException ignored) { }
        }
    }

    @Transactional
    public void markAllRead(UUID adminId) {
        for (AdminNotificationResponse notification : list(adminId)) markRead(adminId, notification.key());
    }

    private static String key(ShopOrder order) {
        return "REQUESTED".equals(order.getReturnStatus())
                ? "RETURN:" + order.getId() + ":REQUESTED"
                : "ORDER:" + order.getId() + ":" + order.getStatus().name();
    }

    private static AdminNotificationResponse response(ShopOrder order, boolean read) {
        boolean returned = "REQUESTED".equals(order.getReturnStatus());
        return new AdminNotificationResponse(
                key(order), returned ? "RETURN_REQUEST" : "ORDER",
                returned ? "Yêu cầu trả hàng" : "Đơn hàng cần xử lý",
                "#" + order.getId().toString().substring(0, 8).toUpperCase() + " · " + order.getShippingPhone(),
                "/orders/" + order.getId(),
                returned && order.getReturnRequestedAt() != null ? order.getReturnRequestedAt() : order.getCreatedAt(),
                read);
    }
}
