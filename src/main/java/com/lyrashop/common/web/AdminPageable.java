package com.lyrashop.common.web;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class AdminPageable {
    private AdminPageable() { }

    public static Pageable of(int page, int size, String sort, String direction,
            Set<String> allowedSorts, String defaultSort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        String safeSort = allowedSorts.contains(sort) ? sort : defaultSort;
        Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize,
                Sort.by(safeDirection, safeSort).and(Sort.by("id")));
    }
}
