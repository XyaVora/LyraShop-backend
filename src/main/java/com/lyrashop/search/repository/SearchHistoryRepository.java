package com.lyrashop.search.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.search.entity.SearchHistory;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findTop10ByUserIdOrderBySearchedAtDescIdDesc(UUID userId);
    List<SearchHistory> findAllByUserIdOrderBySearchedAtDescIdDesc(UUID userId);
    Optional<SearchHistory> findByUserIdAndQuery(UUID userId, String query);
    void deleteAllByUserId(UUID userId);
    long deleteByUserIdAndQuery(UUID userId, String query);
}
