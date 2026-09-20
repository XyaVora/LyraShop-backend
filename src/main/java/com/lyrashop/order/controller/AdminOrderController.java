package com.lyrashop.order.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.order.dto.OrderResponse;
import com.lyrashop.order.dto.UpdateOrderStatusRequest;
import com.lyrashop.order.dto.UpdateTrackingRequest;
import com.lyrashop.order.dto.TrackingEventRequest;
import com.lyrashop.order.service.OrderNotFoundException;
import com.lyrashop.order.service.OrderService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderResponse> list() {
        return orderService.listAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse get(@PathVariable String id) {
        return orderService.get(orderId(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateStatus(orderId(id), request.status());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}/tracking", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updateTracking(@PathVariable String id,
            @Valid @RequestBody UpdateTrackingRequest request) {
        return orderService.updateTracking(orderId(id), request.carrier(), request.trackingCode(),
                request.trackingUrl(), request.estimatedDeliveryAt());
    }
    @PreAuthorize("hasRole('ADMIN')") @PostMapping(path="/{id}/tracking-events",consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
    public java.util.Map<String,Object> addTracking(@PathVariable String id,@Valid @RequestBody TrackingEventRequest request){return orderService.addTrackingEvent(orderId(id),request);}

    private static UUID orderId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new OrderNotFoundException();
        }
    }
}
