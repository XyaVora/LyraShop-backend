package com.lyrashop.order.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.lyrashop.dashboard.repository.BestSellerProjection;
import com.lyrashop.dashboard.repository.OrderStatusCount;
import com.lyrashop.order.entity.ShopOrder;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, UUID> {

    List<ShopOrder> findAllByUserIdOrderByCreatedAtDescIdDesc(UUID userId);

    List<ShopOrder> findAllByOrderByCreatedAtDescIdDesc();

    Optional<ShopOrder> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select shopOrder from ShopOrder shopOrder where shopOrder.id = :id")
    Optional<ShopOrder> findForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select shopOrder from ShopOrder shopOrder where shopOrder.id = :id and shopOrder.userId = :userId")
    Optional<ShopOrder> findForUpdateByUser(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("""
            select count(item) > 0
            from ShopOrder shopOrder
            join shopOrder.items item
            where shopOrder.userId = :userId
              and shopOrder.status = com.lyrashop.order.entity.OrderStatus.DELIVERED
              and item.productId = :productId
            """)
    boolean hasDeliveredProduct(@Param("userId") UUID userId, @Param("productId") UUID productId);

    @Query("""
            select coalesce(sum(shopOrder.totalAmount), 0)
            from ShopOrder shopOrder
            where shopOrder.paymentStatus = com.lyrashop.order.entity.PaymentStatus.PAID
            """)
    BigDecimal sumPaidTotal();
    @Query("select coalesce(sum(o.totalAmount),0) from ShopOrder o where o.userId=:userId and o.paymentStatus=com.lyrashop.order.entity.PaymentStatus.PAID")
    BigDecimal sumPaidTotalByUserId(@Param("userId") UUID userId);

    @Query("""
            select count(shopOrder)
            from ShopOrder shopOrder
            where shopOrder.paymentStatus = com.lyrashop.order.entity.PaymentStatus.PAID
            """)
    long countPaid();

    @Query("""
            select shopOrder.status as status, count(shopOrder) as total
            from ShopOrder shopOrder
            group by shopOrder.status
            """)
    List<OrderStatusCount> countGroupedByStatus();

    @Query("""
            select item.productId as productId,
                   min(item.productName) as productName,
                   coalesce(sum(item.quantity), 0) as quantitySold,
                   coalesce(sum(item.subtotal), 0) as revenue
            from ShopOrder shopOrder
            join shopOrder.items item
            where shopOrder.status <> com.lyrashop.order.entity.OrderStatus.CANCELLED
            group by item.productId
            order by sum(item.quantity) desc, sum(item.subtotal) desc, item.productId asc
            """)
    List<BestSellerProjection> findBestSellers(Pageable pageable);
}
