package com.lyrashop.user.entity;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "binary(16)")
    private UUID id;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false, updatable = false, length = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean")
    private boolean active;

    @Column(name = "email_verified", nullable = false, columnDefinition = "boolean")
    private boolean emailVerified;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6)")
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private Instant updatedAt;

    protected User() {
    }

    private User(String email, String passwordHash, String fullName, String phone) {
        this.email = canonicalizeEmail(email);
        this.passwordHash = requireOpaqueText(passwordHash, "passwordHash", 255);
        this.fullName = requireText(fullName, "fullName", 255);
        this.phone = normalizeNullable(phone, "phone", 20);
        this.role = UserRole.CUSTOMER;
        this.active = true;
        this.emailVerified = true;
    }

    public static User createCustomer(String email, String passwordHash, String fullName, String phone) {
        return new User(email, passwordHash, fullName, phone);
    }

    public static User createUnverifiedCustomer(String email, String passwordHash, String fullName, String phone) {
        User user = new User(email, passwordHash, fullName, phone);
        user.emailVerified = false;
        return user;
    }

    public static User createAdmin(String email, String passwordHash, String fullName, String phone) {
        User user = new User(email, passwordHash, fullName, phone);
        user.role = UserRole.ADMIN;
        return user;
    }

    public void assignRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role is required");
        }
        this.role = role;
    }

    public void updateProfile(String fullName, String phone) {
        this.fullName = requireText(fullName, "fullName", 255);
        this.phone = normalizeNullable(phone, "phone", 20);
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = requireOpaqueText(passwordHash, "passwordHash", 255);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEmailVerified() { return emailVerified; }

    public void verifyEmail() { emailVerified = true; }

    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static String canonicalizeEmail(String value) {
        String normalized = Normalizer.normalize(requireText(value, "email", 255), Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("email must not exceed 255 characters");
        }
        return normalized;
    }

    private static String requireOpaqueText(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return value;
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static String normalizeNullable(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(value, fieldName, maxLength);
    }
}
