package com.lyrashop.dashboard.entity;

import static org.hibernate.type.SqlTypes.BINARY;

import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_notification_reads")
public class AdminNotificationRead {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @JdbcTypeCode(BINARY) @Column(name = "admin_id", nullable = false, length = 16) private UUID adminId;
    @Column(name = "notification_key", nullable = false, length = 120) private String notificationKey;
    @CreationTimestamp(source = SourceType.DB) @Column(name = "read_at", nullable = false) private Instant readAt;

    protected AdminNotificationRead() { }
    public AdminNotificationRead(UUID adminId, String notificationKey) {
        this.adminId = adminId;
        this.notificationKey = notificationKey;
    }
    public UUID getAdminId() { return adminId; }
    public String getNotificationKey() { return notificationKey; }
    public Instant getReadAt() { return readAt; }
}
