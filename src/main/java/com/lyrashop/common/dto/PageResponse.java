package com.lyrashop.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public PageResponse { content = List.copyOf(content); }

    public static <S, T> PageResponse<T> from(Page<S> source, List<T> content) {
        return new PageResponse<>(content, source.getNumber(), source.getSize(),
                source.getTotalElements(), source.getTotalPages());
    }
}
