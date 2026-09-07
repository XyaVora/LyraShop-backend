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

    public void assignTotal(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void cancel() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("only pending orders can be cancelled");
        }
        status = OrderStatus.CANCELLED;
    }

    public void transitionTo(OrderStatus next) {
        OrderStatus expected = switch (status) {
            case PENDING -> OrderStatus.CONFIRMED;
            case CONFIRMED -> OrderStatus.PROCESSING;
            case PROCESSING -> OrderStatus.SHIPPING;
            case SHIPPING -> OrderStatus.DELIVERED;
            default -> null;
        };
        if (expected == null || next != expected) {
            throw new IllegalStateException("invalid order status transition");
        }
        status = next;
        if (next == OrderStatus.DELIVERED && paymentMethod == PaymentMethod.COD) {
            paymentStatus = PaymentStatus.PAID;
        }
    }

    public void markPaid() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("cancelled orders cannot be paid");
        }
        paymentStatus = PaymentStatus.PAID;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getShippingAddress() { return shippingAddress; }
    public String getShippingPhone() { return shippingPhone; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
}
