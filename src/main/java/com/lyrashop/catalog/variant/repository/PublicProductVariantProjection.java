package com.lyrashop.catalog.variant.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface PublicProductVariantProjection {

    UUID getId();

    String getSku();

    String getSize();

    String getColor();

    BigDecimal getPrice();

    int getStock();
}
