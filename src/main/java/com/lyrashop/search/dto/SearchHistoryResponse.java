package com.lyrashop.search.dto;

import java.time.Instant;

import com.lyrashop.search.entity.SearchHistory;

public record SearchHistoryResponse(String query, Instant searchedAt) {
    public static SearchHistoryResponse from(SearchHistory item) {
        return new SearchHistoryResponse(item.getQuery(), item.getSearchedAt());
    }
}
