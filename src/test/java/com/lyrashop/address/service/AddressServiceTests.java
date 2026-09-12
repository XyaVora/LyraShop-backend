package com.lyrashop.address.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lyrashop.address.dto.AddressRequest;
import com.lyrashop.address.entity.ShippingAddress;
import com.lyrashop.address.repository.ShippingAddressRepository;

@ExtendWith(MockitoExtension.class)
class AddressServiceTests {
    @Mock ShippingAddressRepository addresses;
    private AddressService service;

    @BeforeEach void setUp() { service = new AddressService(addresses); }

    @Test void makesTheFirstAddressDefault() {
        UUID userId = UUID.randomUUID();
        AddressRequest request = new AddressRequest("Nguyen Van A", "0912345678", "1 Le Loi", null,
                "Quan 1", "TP HCM", false);
        when(addresses.countByUserId(userId)).thenReturn(0L);
        when(addresses.saveAndFlush(any(ShippingAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(userId, request);

        assertThat(result.isDefault()).isTrue();
        verify(addresses).clearDefault(userId);
    }

    @Test void neverReturnsAnAddressOwnedByAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        when(addresses.findByIdAndUserId(addressId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setDefault(userId, addressId))
                .isInstanceOf(AddressNotFoundException.class);
    }
}
