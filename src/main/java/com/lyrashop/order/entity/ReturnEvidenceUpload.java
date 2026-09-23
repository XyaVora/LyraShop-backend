package com.lyrashop.order.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "return_evidence_uploads")
public class ReturnEvidenceUpload {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16) private UUID id;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "user_id", nullable = false, updatable = false, length = 16) private UUID userId;
    @Column(nullable = false, updatable = false, length = 255, unique = true) private String url;
    @Column(name = "expires_at", nullable = false, updatable = false) private Instant expiresAt;
    @Column(name = "consumed_at") private Instant consumedAt;
    @CreationTimestamp(source = SourceType.DB) @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected ReturnEvidenceUpload() {}
    private ReturnEvidenceUpload(UUID userId, String url, Instant expiresAt) { this.userId=userId;this.url=url;this.expiresAt=expiresAt; }
    public static ReturnEvidenceUpload create(UUID userId,String url,Instant expiresAt){return new ReturnEvidenceUpload(userId,url,expiresAt);}
    public UUID getId(){return id;} public String getUrl(){return url;}
    public void consume(){if(consumedAt!=null||!expiresAt.isAfter(Instant.now()))throw new IllegalStateException("upload is inactive");consumedAt=Instant.now();}
}
