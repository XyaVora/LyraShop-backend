package com.lyrashop.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModerateReviewRequest(
        @NotBlank @Pattern(regexp = "PUBLISHED|HIDDEN") String status,
        @Size(max = 500) String note
) { }
