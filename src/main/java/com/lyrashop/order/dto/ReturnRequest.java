package com.lyrashop.order.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReturnRequest(
        @NotBlank @Size(max = 1000) String reason,
        @NotEmpty @Size(max = 100) List<@Valid Item> items,
        @Size(max = 5) List<@Pattern(
                regexp = "(?:https://[^\\s]+|/api/v1/files/[0-9a-f-]{36}\\.(?:jpg|png|webp))",
                message = "must be an https URL or an uploaded image URL"
        ) String> evidenceUrls
) {
    public ReturnRequest {
        reason = reason == null ? null : reason.strip();
        items = items == null ? List.of() : List.copyOf(items);
        evidenceUrls = evidenceUrls == null ? List.of() : evidenceUrls.stream().map(String::strip).toList();
    }
    public record Item(@NotNull Long orderItemId, @Min(1) int quantity) {}
}
