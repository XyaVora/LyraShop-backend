package com.lyrashop.search.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.search.dto.SearchHistoryResponse;
import com.lyrashop.search.entity.SearchHistory;
import com.lyrashop.search.repository.SearchHistoryRepository;

@Service
public class SearchHistoryService {
    private final SearchHistoryRepository repository;
    public SearchHistoryService(SearchHistoryRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponse> list(UUID userId) {
        return repository.findTop10ByUserIdOrderBySearchedAtDescIdDesc(userId).stream()
                .map(SearchHistoryResponse::from).toList();
    }

    @Transactional
    public SearchHistoryResponse add(UUID userId, String rawQuery) {
        String query = rawQuery.strip();
        repository.findByUserIdAndQuery(userId, query).ifPresent(repository::delete);
        repository.flush();
        SearchHistory saved = repository.saveAndFlush(SearchHistory.create(userId, query));
        var entries = repository.findAllByUserIdOrderBySearchedAtDescIdDesc(userId);
        if (entries.size() > 10) repository.deleteAll(entries.subList(10, entries.size()));
        return SearchHistoryResponse.from(saved);
    }

    @Transactional
    public void clear(UUID userId) { repository.deleteAllByUserId(userId); }

    @Transactional
    public void remove(UUID userId, String query) {
        if (query != null && !query.isBlank()) repository.deleteByUserIdAndQuery(userId, query.trim());
    }
}
