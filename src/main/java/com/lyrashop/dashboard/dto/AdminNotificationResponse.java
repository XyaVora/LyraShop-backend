package com.lyrashop.dashboard.dto;

import java.time.Instant;

public record AdminNotificationResponse(
        String key, String type, String title, String message, String path, Instant createdAt, boolean read
) { }
