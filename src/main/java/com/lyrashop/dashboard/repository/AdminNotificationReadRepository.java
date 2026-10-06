package com.lyrashop.dashboard.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lyrashop.dashboard.entity.AdminNotificationRead;

public interface AdminNotificationReadRepository extends JpaRepository<AdminNotificationRead, Long> {
    List<AdminNotificationRead> findAllByAdminIdAndNotificationKeyIn(UUID adminId, Collection<String> keys);
    boolean existsByAdminIdAndNotificationKey(UUID adminId, String key);
}
