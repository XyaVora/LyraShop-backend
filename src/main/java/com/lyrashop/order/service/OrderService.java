package com.lyrashop.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.cart.entity.Cart;
import com.lyrashop.cart.entity.CartItem;
import com.lyrashop.cart.repository.CartItemRepository;
import com.lyrashop.cart.repository.CartRepository;
import com.lyrashop.cart.service.InsufficientStockException;
import com.lyrashop.catalog.category.repository.CategoryRepository;
import com.lyrashop.catalog.product.entity.Product;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.entity.ProductVariant;
import com.lyrashop.catalog.variant.repository.ProductVariantRepository;
import com.lyrashop.catalog.variant.service.VariantNotFoundException;
import com.lyrashop.order.dto.CreateOrderRequest;
import com.lyrashop.order.dto.OrderResponse;
import com.lyrashop.order.dto.VnpayIpnResponse;
import com.lyrashop.order.dto.ReturnRequest;
import com.lyrashop.order.dto.ReturnRequestResponse;
import com.lyrashop.order.entity.CustomerReturnRequest;
import com.lyrashop.order.entity.CustomerReturnItem;
import com.lyrashop.order.entity.CustomerReturnEvidence;
import com.lyrashop.order.repository.CustomerReturnRequestRepository;
import com.lyrashop.order.repository.CustomerReturnItemRepository;
import com.lyrashop.order.repository.CustomerReturnEvidenceRepository;
import com.lyrashop.order.entity.OrderItem;
import com.lyrashop.order.entity.OrderStatus;
import com.lyrashop.order.entity.PaymentMethod;
import com.lyrashop.order.entity.PaymentStatus;
import com.lyrashop.order.entity.ShopOrder;
import com.lyrashop.order.repository.ShopOrderRepository;
import com.lyrashop.promotion.service.PromotionService;
import com.lyrashop.cart.service.StorePricing;
import com.lyrashop.user.service.LoyaltyService;
import com.lyrashop.config.OrderProperties;

@Service
public class OrderService {


    private final ShopOrderRepository orders;
    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final ProductVariantRepository variants;
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final VnpayService vnpay;
    private final PromotionService promotions;
    private final VoucherService vouchers;
    private final com.lyrashop.order.repository.TrackingEventRepository trackingEvents;
    private final LoyaltyService loyalty;
    private final CustomerReturnRequestRepository returnRequests;
    private final CustomerReturnItemRepository returnItems;
    private final CustomerReturnEvidenceRepository returnEvidence;
    private final ReturnEvidenceUploadService evidenceUploads;
    private final OrderProperties orderProperties;

    public OrderService(
            ShopOrderRepository orders,
            CartRepository carts,
            CartItemRepository cartItems,
            ProductVariantRepository variants,
            ProductRepository products,
            CategoryRepository categories,
            VnpayService vnpay,
            PromotionService promotions,
            VoucherService vouchers,
            com.lyrashop.order.repository.TrackingEventRepository trackingEvents,
            LoyaltyService loyalty,
            CustomerReturnRequestRepository returnRequests,
            CustomerReturnItemRepository returnItems,
            CustomerReturnEvidenceRepository returnEvidence,
            ReturnEvidenceUploadService evidenceUploads,
            OrderProperties orderProperties
    ) {
        this.orders = orders;
        this.carts = carts;
        this.cartItems = cartItems;
        this.variants = variants;
        this.products = products;
        this.categories = categories;
        this.vnpay = vnpay;
        this.promotions = promotions;
        this.vouchers = vouchers;
        this.trackingEvents = trackingEvents;
        this.loyalty = loyalty;
        this.returnRequests = returnRequests;
        this.returnItems = returnItems;
        this.returnEvidence = returnEvidence;
        this.evidenceUploads = evidenceUploads;
        this.orderProperties = orderProperties;
    }

    @Transactional
    public OrderResponse create(UUID userId, CreateOrderRequest request, String clientIp, String rawIdempotencyKey) {
        String idempotencyKey = normalizeIdempotencyKey(rawIdempotencyKey);
        if (idempotencyKey != null) {
            ShopOrder existing = orders.findByUserIdAndIdempotencyKey(userId, idempotencyKey).orElse(null);
            if (existing != null) {
                return existing.getPaymentMethod() == PaymentMethod.VNPAY
                        && existing.getPaymentStatus() != PaymentStatus.PAID
                        && existing.getStatus() != OrderStatus.CANCELLED
                        ? OrderResponse.from(existing, vnpay.paymentUrl(existing, clientIp))
                        : OrderResponse.from(existing);
            }
        }
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(request.paymentMethod().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidPaymentMethodException();
        }
        if (paymentMethod == PaymentMethod.VNPAY && !vnpay.enabled()) {
            throw new InvalidPaymentMethodException();
        }
        Cart cart = carts.findByUserId(userId).orElseThrow(EmptyCartException::new);
        List<CartItem> items = cartItems.findAllByCartIdOrderByIdAsc(cart.getId());
        if (items.isEmpty()) {
            throw new EmptyCartException();
        }
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
        BigDecimal discountedSubtotal = BigDecimal.ZERO.setScale(2);
        Map<UUID, BigDecimal> promotionPrices = promotions.activePrices();
        ShopOrder order = ShopOrder.create(
                userId,
                BigDecimal.ZERO.setScale(2),
                paymentMethod,
                request.shippingAddress(),
                request.shippingPhone(),
                request.note()
        );
        order.assignIdempotencyKey(idempotencyKey);
        order.assignExpiration(Instant.now().plus(paymentMethod == PaymentMethod.VNPAY
                ? orderProperties.onlinePaymentTtl()
                : orderProperties.confirmationTtl()));
        for (CartItem item : items) {
            ProductVariant variant = variants.findForStockUpdate(item.getVariantId())
                    .orElseThrow(VariantNotFoundException::new);
            Product product = products.findById(variant.getProductId()).orElseThrow(VariantNotFoundException::new);
            if (!variant.isActive() || !product.isActive()
                    || !categories.existsByIdAndActiveTrue(product.getCategoryId())) {
                throw new VariantNotFoundException();
            }
            if (item.getQuantity() > variant.getStock()) {
                throw new InsufficientStockException();
            }
            variant.decrementStock(item.getQuantity());
            variants.save(variant);
            BigDecimal unitPrice = promotionPrices.getOrDefault(product.getId(), variant.getPrice()).min(variant.getPrice());
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            discountedSubtotal = discountedSubtotal.add(itemSubtotal);
            order.addItem(OrderItem.snapshot(
                    variant.getId(),
                    product.getId(),
                    product.getName(),
                    variant.getSku(),
                    variant.getSize(),
                    variant.getColor(),
                    item.getQuantity(),
                    unitPrice,
                    itemSubtotal
            ));
        }
        var voucher = request.voucherCode() == null
                ? vouchers.noVoucher(discountedSubtotal)
                : vouchers.quoteForOrder(userId, request.voucherCode(), discountedSubtotal);
        BigDecimal giftWrapFee = request.giftWrap() ? StorePricing.giftWrapFee() : BigDecimal.ZERO.setScale(2);
        BigDecimal discount = subtotal.subtract(discountedSubtotal).add(voucher.discountAmount());
        BigDecimal total = voucher.totalAmount().add(giftWrapFee);
        if (request.loyaltyCoins() > LoyaltyService.maximumRedeemable(total)) {
            throw new com.lyrashop.user.service.InvalidLoyaltyRedemptionException();
        }
        order.assignGift(request.giftWrap(), request.giftMessage());
        order.assignPricing(subtotal, discount, voucher.shippingFee(), giftWrapFee, total, voucher.code());
        order.applyLoyalty(request.loyaltyCoins());
        ShopOrder saved = orders.saveAndFlush(order);
        loyalty.spendForOrder(userId, saved, request.loyaltyCoins(), total);
        recordTracking(saved.getId(), "ORDER_PLACED", "Đơn hàng đã được tiếp nhận", null);
        vouchers.recordRedemption(userId, voucher.code(), saved.getId());
        cartItems.deleteAllByCartId(cart.getId());
        if (paymentMethod == PaymentMethod.VNPAY) {
            return OrderResponse.from(saved, vnpay.paymentUrl(saved, clientIp));
        }
        return OrderResponse.from(saved);
    }

    @Transactional
    public VnpayIpnResponse confirmVnpay(Map<String, String> params) {
        if (!vnpay.signatureMatches(params)) {
            return VnpayIpnResponse.invalidSignature();
        }
        UUID orderId;
        try {
            orderId = VnpayService.orderId(params.get("vnp_TxnRef"));
        } catch (OrderNotFoundException exception) {
            return VnpayIpnResponse.orderNotFound();
        }
        ShopOrder order = orders.findForUpdate(orderId).orElse(null);
        if (order == null || order.getPaymentMethod() != PaymentMethod.VNPAY) {
            return VnpayIpnResponse.orderNotFound();
        }
        if (!VnpayService.vndAmount(order.getTotalAmount()).equals(params.get("vnp_Amount"))) {
            return VnpayIpnResponse.invalidAmount();
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return VnpayIpnResponse.alreadyConfirmed();
        }
        if (!"00".equals(params.get("vnp_ResponseCode"))) {
            return VnpayIpnResponse.confirmSuccess();
        }
        try {
            order.markPaid();
        } catch (IllegalStateException exception) {
            return VnpayIpnResponse.orderNotFound();
        }
        order.assignExpiration(Instant.now().plus(orderProperties.confirmationTtl()));
        orders.saveAndFlush(order);
        return VnpayIpnResponse.confirmSuccess();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listForUser(UUID userId) {
        return orders.findAllByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getForUser(UUID userId, UUID orderId) {
        return OrderResponse.from(orders.findByIdAndUserId(orderId, userId)
                .orElseThrow(OrderNotFoundException::new));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAll() {
        return orders.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID orderId) {
        return OrderResponse.from(orders.findById(orderId).orElseThrow(OrderNotFoundException::new));
    }

    @Transactional
    public OrderResponse cancel(UUID userId, UUID orderId, String reason) {
        ShopOrder order = orders.findForUpdateByUser(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
        try {
            order.cancel(reason);
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStatusException();
        }
        restoreStock(order);
        vouchers.release(orderId);
        loyalty.refundCancelledOrder(order);
        ShopOrder saved = orders.saveAndFlush(order);
        recordTracking(orderId, "CANCELLED", "Đơn hàng đã được hủy", null);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse confirmReceived(UUID userId, UUID orderId) {
        ShopOrder order = orders.findForUpdateByUser(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
        try {
            order.confirmReceived();
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStatusException();
        }
        ShopOrder saved = orders.saveAndFlush(order);
        loyalty.awardDeliveredOrder(saved);
        recordTracking(orderId, "DELIVERED", "Khách hàng đã xác nhận nhận hàng", null);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse retryPayment(UUID userId, UUID orderId, String clientIp) {
        ShopOrder order = orders.findForUpdateByUser(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
        if (order.getPaymentMethod() != PaymentMethod.VNPAY
                || order.getPaymentStatus() == PaymentStatus.PAID
                || order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException();
        }
        return OrderResponse.from(order, vnpay.paymentUrl(order, clientIp));
    }

    @Transactional
    public OrderResponse requestReturn(UUID userId, UUID orderId, ReturnRequest request) {
        ShopOrder order = orders.findForUpdateByUser(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
        if (returnRequests.existsByOrderId(orderId)) throw new InvalidOrderStatusException();
        Map<Long, OrderItem> orderItemsById = new HashMap<>();
        order.getItems().forEach(item -> orderItemsById.put(item.getId(), item));
        HashSet<Long> selectedIds = new HashSet<>();
        for (ReturnRequest.Item selected : request.items()) {
            OrderItem item = orderItemsById.get(selected.orderItemId());
            if (item == null || selected.quantity() > item.getQuantity()
                    || !selectedIds.add(selected.orderItemId())) {
                throw new InvalidOrderStatusException();
            }
        }
        try {
            order.requestReturn(request.reason());
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStatusException();
        }
        ShopOrder saved = orders.saveAndFlush(order);
        CustomerReturnRequest savedRequest = returnRequests.saveAndFlush(
                CustomerReturnRequest.create(orderId, userId, request.reason()));
        evidenceUploads.consume(userId, request.evidenceUrls());
        returnItems.saveAll(request.items().stream()
                .map(item -> CustomerReturnItem.create(savedRequest.getId(), item.orderItemId(), item.quantity()))
                .toList());
        returnEvidence.saveAll(request.evidenceUrls().stream()
                .map(url -> CustomerReturnEvidence.create(savedRequest.getId(), url)).toList());
        recordTracking(orderId, "RETURN_REQUESTED", "Khách hàng đã gửi yêu cầu trả hàng", null);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ReturnRequestResponse getReturnRequest(UUID userId, UUID orderId) {
        orders.findByIdAndUserId(orderId, userId).orElseThrow(OrderNotFoundException::new);
        CustomerReturnRequest request = returnRequests.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
        return ReturnRequestResponse.from(request,
                returnItems.findAllByReturnRequestIdOrderByIdAsc(request.getId()),
                returnEvidence.findAllByReturnRequestIdOrderByIdAsc(request.getId()));
    }

    @Transactional
    public OrderResponse cancelReturn(UUID userId, UUID orderId) {
        ShopOrder order = orders.findForUpdateByUser(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
        try {
            order.cancelReturnRequest();
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStatusException();
        }
        returnRequests.findByOrderIdAndUserId(orderId, userId).ifPresent(returnRequest -> {
            List<String> evidenceUrls = returnEvidence.findAllByReturnRequestIdOrderByIdAsc(returnRequest.getId())
                    .stream().map(CustomerReturnEvidence::getUrl).toList();
            returnRequests.delete(returnRequest);
            evidenceUploads.discard(userId, evidenceUrls);
        });
        ShopOrder saved = orders.saveAndFlush(order);
        recordTracking(orderId, "RETURN_CANCELLED", "Khách hàng đã hủy yêu cầu trả hàng", null);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse updateTracking(UUID orderId, String carrier, String code, String url,
            java.time.Instant estimatedDeliveryAt) {
        ShopOrder order = orders.findForUpdate(orderId).orElseThrow(OrderNotFoundException::new);
        order.assignTracking(carrier, code, url, estimatedDeliveryAt);
        ShopOrder saved = orders.saveAndFlush(order);
        recordTracking(orderId, "TRACKING_ASSIGNED", "Đã cập nhật mã vận đơn " + code, carrier);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> tracking(UUID userId,UUID orderId){orders.findByIdAndUserId(orderId,userId).orElseThrow(OrderNotFoundException::new);return trackingEvents.findAllByOrderIdOrderByOccurredAtDesc(orderId).stream().map(e->{Map<String,Object> m=new java.util.LinkedHashMap<>();m.put("id",e.getId());m.put("status",e.getStatus());m.put("description",e.getDescription());m.put("location",e.getLocation());m.put("occurredAt",e.getOccurredAt());return m;}).toList();}
    @Transactional public Map<String,Object> addTrackingEvent(UUID orderId,com.lyrashop.order.dto.TrackingEventRequest r){orders.findById(orderId).orElseThrow(OrderNotFoundException::new);var e=trackingEvents.save(new com.lyrashop.order.entity.TrackingEvent(orderId,r.status(),r.description(),r.location(),r.occurredAt()));return trackingEventResponse(e);}

    @Transactional
    public OrderResponse updateStatus(UUID orderId, String status) {
        OrderStatus next;
        try {
            next = OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidOrderStatusException();
        }
        ShopOrder order = orders.findForUpdate(orderId).orElseThrow(OrderNotFoundException::new);
        try {
            order.transitionTo(next);
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStatusException();
        }
        ShopOrder saved = orders.saveAndFlush(order);
        if (next == OrderStatus.DELIVERED) loyalty.awardDeliveredOrder(saved);
        recordTracking(orderId, next.name(), statusDescription(next), null);
        return OrderResponse.from(saved);
    }

    @Transactional
    public void expirePending(UUID orderId, Instant now) {
        ShopOrder order = orders.findForUpdate(orderId).orElse(null);
        if (order == null || !order.isExpired(now)) return;
        order.expire(now);
        restoreStock(order);
        vouchers.release(orderId);
        loyalty.refundCancelledOrder(order);
        orders.saveAndFlush(order);
        recordTracking(orderId, "EXPIRED", "Đơn hàng đã tự động hết hạn", null);
    }

    private void restoreStock(ShopOrder order) {
        for (var item : order.getItems()) {
            ProductVariant variant = variants.findForStockUpdate(item.getVariantId())
                    .orElseThrow(VariantNotFoundException::new);
            variant.incrementStock(item.getQuantity());
            variants.save(variant);
        }
    }

    private void recordTracking(UUID orderId, String status, String description, String location) {
        trackingEvents.save(new com.lyrashop.order.entity.TrackingEvent(
                orderId, status, description, location, Instant.now()
        ));
    }

    private static Map<String, Object> trackingEventResponse(com.lyrashop.order.entity.TrackingEvent event) {
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("id", event.getId());
        response.put("status", event.getStatus());
        response.put("description", event.getDescription());
        response.put("location", event.getLocation());
        response.put("occurredAt", event.getOccurredAt());
        return response;
    }

    private static String statusDescription(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Người bán đã xác nhận, đơn hàng đang chờ lấy hàng";
            case PROCESSING -> "Đơn hàng đã được đóng gói và đang chờ lấy hàng";
            case SHIPPING -> "Đơn hàng đã được bàn giao cho đơn vị vận chuyển";
            case DELIVERED -> "Đơn hàng đã được giao thành công";
            case CANCELLED -> "Đơn hàng đã được hủy";
            default -> "Trạng thái đơn hàng đã được cập nhật";
        };
    }

    private static String normalizeIdempotencyKey(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.strip();
        if (normalized.length() < 16 || normalized.length() > 64
                || !normalized.matches("[A-Za-z0-9_-]+")) {
            throw new InvalidIdempotencyKeyException();
        }
        return normalized;
    }

}
