package com.lyrashop.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
import com.lyrashop.order.entity.OrderItem;
import com.lyrashop.order.entity.OrderStatus;
import com.lyrashop.order.entity.PaymentMethod;
import com.lyrashop.order.entity.PaymentStatus;
import com.lyrashop.order.entity.ShopOrder;
import com.lyrashop.order.repository.ShopOrderRepository;

@Service
public class OrderService {

    private final ShopOrderRepository orders;
    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final ProductVariantRepository variants;
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final VnpayService vnpay;

    public OrderService(
            ShopOrderRepository orders,
            CartRepository carts,
            CartItemRepository cartItems,
            ProductVariantRepository variants,
            ProductRepository products,
            CategoryRepository categories,
            VnpayService vnpay
    ) {
        this.orders = orders;
        this.carts = carts;
        this.cartItems = cartItems;
        this.variants = variants;
        this.products = products;
        this.categories = categories;
        this.vnpay = vnpay;
    }

    @Transactional
    public OrderResponse create(UUID userId, CreateOrderRequest request, String clientIp) {
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
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        ShopOrder order = ShopOrder.create(
                userId,
                total,
                paymentMethod,
                request.shippingAddress(),
                request.shippingPhone(),
                request.note()
        );
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
            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            order.addItem(OrderItem.snapshot(
                    variant.getId(),
                    product.getName(),
                    variant.getSku(),
                    variant.getSize(),
                    variant.getColor(),
                    item.getQuantity(),
                    variant.getPrice(),
                    subtotal
            ));
        }
        order.assignTotal(total);
        ShopOrder saved = orders.saveAndFlush(order);
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
    public OrderResponse cancel(UUID userId, UUID orderId) {
        ShopOrder order = orders.findForUpdateByUser(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
        try {
            order.cancel();
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStatusException();
        }
        restoreStock(order);
        return OrderResponse.from(orders.saveAndFlush(order));
    }

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
        return OrderResponse.from(orders.saveAndFlush(order));
    }

    private void restoreStock(ShopOrder order) {
        for (var item : order.getItems()) {
            ProductVariant variant = variants.findForStockUpdate(item.getVariantId())
                    .orElseThrow(VariantNotFoundException::new);
            variant.incrementStock(item.getQuantity());
            variants.save(variant);
        }
    }

}

