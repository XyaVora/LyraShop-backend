package com.lyrashop.address.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.address.dto.AddressRequest;
import com.lyrashop.address.dto.AddressResponse;
import com.lyrashop.address.service.AddressService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/addresses")
@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
public class AddressController {
    private final AddressService service;
    public AddressController(AddressService service) { this.service = service; }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AddressResponse> list(Authentication auth) { return service.list(userId(auth)); }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddressResponse> create(Authentication auth, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId(auth), request));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AddressResponse update(Authentication auth, @PathVariable UUID id, @Valid @RequestBody AddressRequest request) {
        return service.update(userId(auth), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID id) {
        service.delete(userId(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{id}/default", produces = MediaType.APPLICATION_JSON_VALUE)
    public AddressResponse setDefault(Authentication auth, @PathVariable UUID id) {
        return service.setDefault(userId(auth), id);
    }

    private static UUID userId(Authentication auth) { return UUID.fromString(auth.getName()); }
}
