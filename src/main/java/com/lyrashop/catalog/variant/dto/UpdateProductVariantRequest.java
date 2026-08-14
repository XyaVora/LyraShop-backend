package com.lyrashop.catalog.variant.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record UpdateProductVariantRequest(
 @NotBlank @Size(max=100) @Pattern(regexp="[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*") String sku,
 @NotBlank @Size(max=20) String size,
 @NotBlank @Size(max=50) String color,
 @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal price,
 @NotNull @PositiveOrZero Long version
) {
 public UpdateProductVariantRequest { sku=strip(sku); size=strip(size); color=strip(color); }
 private static String strip(String value){ return value==null?null:value.strip(); }
}
