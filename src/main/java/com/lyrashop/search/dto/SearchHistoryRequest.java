package com.lyrashop.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchHistoryRequest(
        @NotBlank @Size(max = 100) String query
) {}
