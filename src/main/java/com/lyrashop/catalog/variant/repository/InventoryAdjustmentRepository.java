package com.lyrashop.catalog.variant.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lyrashop.catalog.variant.entity.InventoryAdjustment;
public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment,Long>{List<InventoryAdjustment> findAllByProductIdOrderByCreatedAtDescIdDesc(UUID productId);}
