package com.lyrashop.order.repository;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.lyrashop.order.entity.CustomerReturnItem;
public interface CustomerReturnItemRepository extends JpaRepository<CustomerReturnItem,Long>{List<CustomerReturnItem> findAllByReturnRequestIdOrderByIdAsc(UUID returnRequestId);}
