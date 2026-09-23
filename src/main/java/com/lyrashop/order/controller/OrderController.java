package com.lyrashop.order.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import com.lyrashop.order.dto.CreateOrderRequest;
import com.lyrashop.order.dto.CancelOrderRequest;
import com.lyrashop.order.dto.OrderResponse;
import com.lyrashop.order.dto.ReturnRequest;
import com.lyrashop.order.dto.ReturnRequestResponse;
import com.lyrashop.order.service.OrderNotFoundException;
import com.lyrashop.order.service.OrderService;
import com.lyrashop.order.service.ReturnEvidenceUploadService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final ReturnEvidenceUploadService evidenceUploads;

    public OrderController(OrderService orderService, ReturnEvidenceUploadService evidenceUploads) {
        this.orderService = orderService;
        this.evidenceUploads = evidenceUploads;
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderResponse> list(Authentication authentication) {
        return orderService.listForUser(userId(authentication));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(userId(authentication), request, httpRequest.getRemoteAddr(), idempotencyKey));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping(path = "/return-evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<java.util.Map<String, String>> uploadReturnEvidence(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("url", evidenceUploads.store(userId(authentication), file)));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse get(Authentication authentication, @PathVariable String id) {
        return orderService.getForUser(userId(authentication), orderId(id));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PutMapping(path = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse cancel(Authentication authentication, @PathVariable String id,
            @Valid @RequestBody CancelOrderRequest request) {
        return orderService.cancel(userId(authentication), orderId(id), request.reason());
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PutMapping(path = "/{id}/confirm-received", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse confirmReceived(Authentication authentication, @PathVariable String id) {
        return orderService.confirmReceived(userId(authentication), orderId(id));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping(path = "/{id}/retry-payment", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse retryPayment(Authentication authentication, @PathVariable String id,
            HttpServletRequest request) {
        return orderService.retryPayment(userId(authentication), orderId(id), request.getRemoteAddr());
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping(path = "/{id}/return-request", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse requestReturn(Authentication authentication, @PathVariable String id,
            @Valid @RequestBody ReturnRequest request) {
        return orderService.requestReturn(userId(authentication), orderId(id), request);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping(path = "/{id}/return-request", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnRequestResponse getReturnRequest(Authentication authentication, @PathVariable String id) {
        return orderService.getReturnRequest(userId(authentication), orderId(id));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PutMapping(path = "/{id}/return-request/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse cancelReturn(Authentication authentication, @PathVariable String id) {
        return orderService.cancelReturn(userId(authentication), orderId(id));
    }
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')") @GetMapping(path="/{id}/tracking-events",produces=MediaType.APPLICATION_JSON_VALUE)
    public java.util.List<java.util.Map<String,Object>> tracking(Authentication a,@PathVariable String id){return orderService.tracking(userId(a),orderId(id));}

    private static UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private static UUID orderId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new OrderNotFoundException();
        }
    }
}
