package com.lyrashop.search.entity;

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

import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @JdbcTypeCode(BINARY) @Column(name = "user_id", nullable = false, length = 16) private UUID userId;
    @Column(name = "query", nullable = false, length = 100) private String query;
    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "searched_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant searchedAt;
    protected SearchHistory() {}
    private SearchHistory(UUID userId, String query) { this.userId = userId; this.query = query; }
    public static SearchHistory create(UUID userId, String query) { return new SearchHistory(userId, query); }
    public Long getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getQuery() { return query; }
    public Instant getSearchedAt() { return searchedAt; }
}
