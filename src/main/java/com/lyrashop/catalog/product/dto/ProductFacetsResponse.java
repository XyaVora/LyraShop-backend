package com.lyrashop.catalog.product.dto;

import java.util.List;

public record ProductFacetsResponse(List<String> colors, List<String> sizes) {}
