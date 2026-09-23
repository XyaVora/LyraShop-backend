package com.lyrashop.order.repository;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.lyrashop.order.entity.CustomerReturnEvidence;
public interface CustomerReturnEvidenceRepository extends JpaRepository<CustomerReturnEvidence,Long>{List<CustomerReturnEvidence> findAllByReturnRequestIdOrderByIdAsc(UUID returnRequestId);}
