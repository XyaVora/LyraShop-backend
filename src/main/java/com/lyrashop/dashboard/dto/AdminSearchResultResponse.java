package com.lyrashop.dashboard.dto;

import java.util.UUID;

public record AdminSearchResultResponse(
        String type,
        UUID id,
        String title,
        String subtitle,
        String path
) {
}
