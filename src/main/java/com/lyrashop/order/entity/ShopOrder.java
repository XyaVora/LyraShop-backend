package com.lyrashop.order.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;
import static org.hibernate.type.SqlTypes.DECIMAL;

@Entity
@Table(name = "orders")
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(BINARY)
    @Column(name = "id", nullable = false, updatable = false, length = 16)
    private UUID id;

    @JdbcTypeCode(BINARY)
    @Column(name = "user_id", nullable = false, updatable = false, length = 16)
    private UUID userId;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "loyalty_coins_used", nullable = false)
    private long loyaltyCoinsUsed;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "loyalty_discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal loyaltyDiscountAmount;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "gift_wrap_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal giftWrapFee;

    @JdbcTypeCode(DECIMAL)
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "text")
    private String shippingAddress;

    @Column(name = "shipping_phone", nullable = false, length = 20)
    private String shippingPhone;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "voucher_code", length = 30)
    private String voucherCode;

    @Column(name = "gift_wrap", nullable = false)
    private boolean giftWrap;

    @Column(name = "gift_message", length = 500)
    private String giftMessage;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "shipping_carrier", length = 100)
    private String shippingCarrier;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "tracking_url", length = 500)
    private String trackingUrl;

    @Column(name = "estimated_delivery_at")
    private Instant estimatedDeliveryAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "return_status", length = 30)
    private String returnStatus;

    @Column(name = "return_reason", length = 1000)
    private String returnReason;

    @Column(name = "return_requested_at")
    private Instant returnRequestedAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    protected ShopOrder() {
    }

    private ShopOrder(
            UUID userId,
            BigDecimal totalAmount,
            PaymentMethod paymentMethod,
            String shippingAddress,
            String shippingPhone,
            String note
    ) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.subtotalAmount = totalAmount;
        this.discountAmount = BigDecimal.ZERO.setScale(2);
        this.loyaltyDiscountAmount = BigDecimal.ZERO.setScale(2);
        this.shippingFee = BigDecimal.ZERO.setScale(2);
        this.giftWrapFee = BigDecimal.ZERO.setScale(2);
        this.status = OrderStatus.PENDING;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.shippingAddress = shippingAddress;
        this.shippingPhone = shippingPhone;
        this.note = note;
    }

    public static ShopOrder create(
            UUID userId,
            BigDecimal totalAmount,
            PaymentMethod paymentMethod,
            String shippingAddress,
            String shippingPhone,
            String note
    ) {
        return new ShopOrder(userId, totalAmount, paymentMethod, shippingAddress, shippingPhone, note);
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void assignIdempotencyKey(String value) {
        this.idempotencyKey = value;
    }

    public void assignExpiration(Instant value) {
        this.expiresAt = value;
    }

    public void assignPricing(BigDecimal subtotalAmount, BigDecimal discountAmount,
            BigDecimal shippingFee, BigDecimal giftWrapFee, BigDecimal totalAmount, String voucherCode) {
        this.subtotalAmount = subtotalAmount;
        this.discountAmount = discountAmount;
        this.shippingFee = shippingFee;
        this.giftWrapFee = giftWrapFee;
        this.totalAmount = totalAmount;
        this.voucherCode = voucherCode;
    }

    public void assignGift(boolean enabled, String message) {
        this.giftWrap = enabled;
        this.giftMessage = enabled ? message : null;
    }

    public void applyLoyalty(long coins) {
        if (coins < 0) throw new IllegalArgumentException("loyalty coins must be non-negative");
        BigDecimal amount = BigDecimal.valueOf(coins).setScale(2);
        if (amount.compareTo(totalAmount) > 0) throw new IllegalArgumentException("loyalty discount exceeds total");
        this.loyaltyCoinsUsed = coins;
        this.loyaltyDiscountAmount = amount;
        this.discountAmount = this.discountAmount.add(amount);
        this.totalAmount = this.totalAmount.subtract(amount);
    }

    public void cancel(String reason) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("only pending orders can be cancelled");
        }
        status = OrderStatus.CANCELLED;
        cancellationReason = reason;
    }

    public void cancel() {
        cancel("Khách hàng yêu cầu hủy đơn");
    }

    public void transitionTo(OrderStatus next) {
        OrderStatus expected = switch (status) {
            case PENDING -> OrderStatus.CONFIRMED;
            case CONFIRMED -> OrderStatus.PROCESSING;
            case PROCESSING -> OrderStatus.SHIPPING;
            case SHIPPING -> OrderStatus.DELIVERED;
            default -> null;
        };
        if (status == OrderStatus.PENDING
                && next == OrderStatus.CONFIRMED
                && paymentMethod != PaymentMethod.COD
                && paymentStatus != PaymentStatus.PAID) {
            throw new IllegalStateException("online payment must be completed before confirmation");
        }
        if (expected == null || next != expected) {
            throw new IllegalStateException("invalid order status transition");
        }
        status = next;
        if (next == OrderStatus.CONFIRMED) {
            expiresAt = null;
        }
        if (next == OrderStatus.DELIVERED && paymentMethod == PaymentMethod.COD) {
            paymentStatus = PaymentStatus.PAID;
        }
        if (next == OrderStatus.DELIVERED) {
            deliveredAt = Instant.now();
        }
    }

    public void confirmReceived() {
        if (status != OrderStatus.SHIPPING) {
            throw new IllegalStateException("only shipping orders can be confirmed as received");
        }
        transitionTo(OrderStatus.DELIVERED);
    }

    public void assignTracking(String carrier, String code, String url, Instant estimatedDeliveryAt) {
        this.shippingCarrier = carrier;
        this.trackingCode = code;
        this.trackingUrl = url;
        this.estimatedDeliveryAt = estimatedDeliveryAt;
    }

    public void requestReturn(String reason) {
        if (status != OrderStatus.DELIVERED || returnStatus != null || deliveredAt == null
                || deliveredAt.isBefore(Instant.now().minus(java.time.Duration.ofDays(30)))) {
            throw new IllegalStateException("return is not allowed");
        }
        returnStatus = "REQUESTED";
        returnReason = reason;
        returnRequestedAt = Instant.now();
    }

    public void cancelReturnRequest() {
        if (!"REQUESTED".equals(returnStatus)) {
            throw new IllegalStateException("return request cannot be cancelled");
        }
        returnStatus = null;
        returnReason = null;
        returnRequestedAt = null;
    }

    public void markPaid() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("cancelled orders cannot be paid");
        }
        paymentStatus = PaymentStatus.PAID;
    }

    public boolean isExpired(Instant now) {
        return status == OrderStatus.PENDING && expiresAt != null && !expiresAt.isAfter(now);
    }

    public void expire(Instant now) {
        if (!isExpired(now)) throw new IllegalStateException("order is not expired");
        status = OrderStatus.CANCELLED;
        cancellationReason = "Đơn hàng tự động hết hạn";
        expiresAt = null;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public long getLoyaltyCoinsUsed() { return loyaltyCoinsUsed; }
    public BigDecimal getLoyaltyDiscountAmount() { return loyaltyDiscountAmount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getGiftWrapFee() { return giftWrapFee; }
    public OrderStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getShippingAddress() { return shippingAddress; }
    public String getShippingPhone() { return shippingPhone; }
    public String getNote() { return note; }
    public String getVoucherCode() { return voucherCode; }
    public boolean isGiftWrap() { return giftWrap; }
    public String getGiftMessage() { return giftMessage; }
    public String getCancellationReason() { return cancellationReason; }
    public String getShippingCarrier() { return shippingCarrier; }
    public String getTrackingCode() { return trackingCode; }
    public String getTrackingUrl() { return trackingUrl; }
    public Instant getEstimatedDeliveryAt() { return estimatedDeliveryAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getReturnStatus() { return returnStatus; }
    public String getReturnReason() { return returnReason; }
    public Instant getReturnRequestedAt() { return returnRequestedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
}
