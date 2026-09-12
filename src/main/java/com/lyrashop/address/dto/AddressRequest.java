package com.lyrashop.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 255) String recipientName,
        @NotBlank @Size(max = 20) @Pattern(regexp = "^(?:\\+?84|0)(?:\\d[ .-]?){8,10}$") String phone,
        @NotBlank @Size(max = 500) String addressLine,
        @Size(max = 255) String ward,
        @NotBlank @Size(max = 255) String district,
        @NotBlank @Size(max = 255) String city,
        boolean isDefault
) {}
