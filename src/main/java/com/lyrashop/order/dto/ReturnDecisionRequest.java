package com.lyrashop.order.dto;

import jakarta.validation.constraints.Size;

public record ReturnDecisionRequest(@Size(max = 500) String note) {}
