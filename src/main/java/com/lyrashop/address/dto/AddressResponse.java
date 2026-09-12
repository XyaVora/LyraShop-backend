package com.lyrashop.address.dto;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.address.entity.ShippingAddress;

public record AddressResponse(UUID id, String recipientName, String phone, String addressLine, String ward,
        String district, String city, boolean isDefault, Instant createdAt, Instant updatedAt) {
    public static AddressResponse from(ShippingAddress address) {
        return new AddressResponse(address.getId(), address.getRecipientName(), address.getPhone(),
                address.getAddressLine(), address.getWard(), address.getDistrict(), address.getCity(),
                address.isDefaultAddress(), address.getCreatedAt(), address.getUpdatedAt());
    }
}
