package com.lyrashop.promotion.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class PromotionProductId implements Serializable {
    private UUID promotionId;
    private UUID productId;

    public PromotionProductId() {}

    public PromotionProductId(UUID promotionId, UUID productId) {
        this.promotionId = promotionId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PromotionProductId that)) return false;
        return Objects.equals(promotionId, that.promotionId)
                && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, productId);
    }
}
