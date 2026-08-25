package com.lyrashop.dashboard.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface BestSellerProjection {

    UUID getProductId();

    String getProductName();

    Long getQuantitySold();

    BigDecimal getRevenue();
}
