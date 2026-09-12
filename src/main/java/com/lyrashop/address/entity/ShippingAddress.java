package com.lyrashop.address.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(name = "shipping_addresses")
public class ShippingAddress {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(BINARY)
    @Column(name = "id", nullable = false, updatable = false, length = 16)
    private UUID id;
    @JdbcTypeCode(BINARY) @Column(name = "user_id", nullable = false, updatable = false, length = 16)
    private UUID userId;
    @Column(name = "recipient_name", nullable = false, length = 255) private String recipientName;
    @Column(name = "phone", nullable = false, length = 20) private String phone;
    @Column(name = "address_line", nullable = false, length = 500) private String addressLine;
    @Column(name = "ward", length = 255) private String ward;
    @Column(name = "district", nullable = false, length = 255) private String district;
    @Column(name = "city", nullable = false, length = 255) private String city;
    @Column(name = "is_default", nullable = false, columnDefinition = "boolean") private boolean defaultAddress;
    @CreationTimestamp(source = SourceType.DB) @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)") private Instant createdAt;
    @UpdateTimestamp(source = SourceType.DB) @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)") private Instant updatedAt;

    protected ShippingAddress() {}
    private ShippingAddress(UUID userId, String recipientName, String phone, String addressLine, String ward, String district, String city, boolean isDefault) {
        this.userId = userId;
        update(recipientName, phone, addressLine, ward, district, city, isDefault);
    }
    public static ShippingAddress create(UUID userId, String recipientName, String phone, String addressLine, String ward, String district, String city, boolean isDefault) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        return new ShippingAddress(userId, recipientName, phone, addressLine, ward, district, city, isDefault);
    }
    public void update(String recipientName, String phone, String addressLine, String ward, String district, String city, boolean isDefault) {
        this.recipientName = required(recipientName, "recipientName", 255);
        this.phone = required(phone, "phone", 20);
        this.addressLine = required(addressLine, "addressLine", 500);
        this.ward = optional(ward, 255);
        this.district = required(district, "district", 255);
        this.city = required(city, "city", 255);
        this.defaultAddress = isDefault;
    }
    public void makeDefault() { this.defaultAddress = true; }
    public void removeDefault() { this.defaultAddress = false; }
    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String clean = value.strip();
        if (clean.length() > max) throw new IllegalArgumentException(field + " is too long");
        return clean;
    }
    private static String optional(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String clean = value.strip();
        if (clean.length() > max) throw new IllegalArgumentException("value is too long");
        return clean;
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getRecipientName() { return recipientName; }
    public String getPhone() { return phone; }
    public String getAddressLine() { return addressLine; }
    public String getWard() { return ward; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
