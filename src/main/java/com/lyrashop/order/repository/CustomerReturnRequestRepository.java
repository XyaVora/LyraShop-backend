package com.lyrashop.order.repository;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.lyrashop.order.entity.CustomerReturnRequest;
public interface CustomerReturnRequestRepository extends JpaRepository<CustomerReturnRequest,UUID>{Optional<CustomerReturnRequest> findByOrderIdAndUserId(UUID orderId,UUID userId);boolean existsByOrderId(UUID orderId);}
