package com.lyrashop.address.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.address.dto.AddressRequest;
import com.lyrashop.address.dto.AddressResponse;
import com.lyrashop.address.entity.ShippingAddress;
import com.lyrashop.address.repository.ShippingAddressRepository;

@Service
public class AddressService {
    private final ShippingAddressRepository addresses;
    public AddressService(ShippingAddressRepository addresses) { this.addresses = addresses; }

    @Transactional(readOnly = true)
    public List<AddressResponse> list(UUID userId) {
        return addresses.findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId).stream()
                .map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse create(UUID userId, AddressRequest request) {
        boolean makeDefault = request.isDefault() || addresses.countByUserId(userId) == 0;
        if (makeDefault) addresses.clearDefault(userId);
        var address = ShippingAddress.create(userId, request.recipientName(), request.phone(), request.addressLine(),
                request.ward(), request.district(), request.city(), makeDefault);
        return AddressResponse.from(addresses.saveAndFlush(address));
    }

    @Transactional
    public AddressResponse update(UUID userId, UUID id, AddressRequest request) {
        ShippingAddress address = require(userId, id);
        if (request.isDefault()) addresses.clearDefault(userId);
        address.update(request.recipientName(), request.phone(), request.addressLine(), request.ward(),
                request.district(), request.city(), request.isDefault());
        return AddressResponse.from(addresses.saveAndFlush(address));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        ShippingAddress address = require(userId, id);
        boolean wasDefault = address.isDefaultAddress();
        addresses.delete(address);
        addresses.flush();
        if (wasDefault) {
            addresses.findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId).stream().findFirst()
                    .ifPresent(next -> { next.makeDefault(); addresses.save(next); });
        }
    }

    @Transactional
    public AddressResponse setDefault(UUID userId, UUID id) {
        ShippingAddress address = require(userId, id);
        addresses.clearDefault(userId);
        address.makeDefault();
        return AddressResponse.from(addresses.saveAndFlush(address));
    }

    private ShippingAddress require(UUID userId, UUID id) {
        return addresses.findByIdAndUserId(id, userId).orElseThrow(AddressNotFoundException::new);
    }
}
