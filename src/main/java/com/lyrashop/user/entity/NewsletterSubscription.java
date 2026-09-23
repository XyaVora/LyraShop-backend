package com.lyrashop.user.entity;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

@Entity
@Table(name = "newsletter_subscriptions")
public class NewsletterSubscription {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16) private UUID id;
    @Column(nullable = false, updatable = false) private String email;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "confirmation_token_hash", length = 32) private byte[] confirmationTokenHash;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "unsubscribe_token_hash", length = 32) private byte[] unsubscribeTokenHash;
    @Column(name = "is_active") private boolean active;
    @Column(name = "confirmed_at") private Instant confirmedAt;
    @Column(name = "confirmation_expires_at") private Instant confirmationExpiresAt;

    protected NewsletterSubscription() {}
    public NewsletterSubscription(String email) { this.email = email; }
    public void beginConfirmation(byte[] confirmationHash, byte[] unsubscribeHash, Instant confirmationExpiresAt) {
        this.confirmationTokenHash = Arrays.copyOf(confirmationHash, confirmationHash.length);
        this.unsubscribeTokenHash = Arrays.copyOf(unsubscribeHash, unsubscribeHash.length);
        this.confirmationExpiresAt = confirmationExpiresAt;
        this.active = false;
        this.confirmedAt = null;
    }
    public void confirm() { this.active = true; this.confirmedAt = Instant.now(); this.confirmationTokenHash = null; this.confirmationExpiresAt = null; }
    public void deactivate() { this.active = false; }
    public String getEmail(){return email;}
}
