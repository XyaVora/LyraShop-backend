package com.lyrashop.messaging.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_outbox")
public class EmailOutbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "from_address", nullable = false, length = 255) private String fromAddress;
    @Column(name = "recipient", nullable = false, length = 255) private String recipient;
    @Column(name = "subject", nullable = false, length = 255) private String subject;
    @Column(name = "body", nullable = false, columnDefinition = "text") private String body;
    @Column(name = "status", nullable = false, length = 20) private String status;
    @Column(name = "attempts", nullable = false) private int attempts;
    @Column(name = "next_attempt_at", nullable = false) private Instant nextAttemptAt;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "last_error", length = 1000) private String lastError;
    @CreationTimestamp(source = SourceType.DB) @Column(name = "created_at") private Instant createdAt;

    protected EmailOutbox() {}

    private EmailOutbox(String fromAddress, String recipient, String subject, String body) {
        this.fromAddress = fromAddress;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.status = "PENDING";
        this.nextAttemptAt = Instant.now();
    }

    public static EmailOutbox create(String fromAddress, String recipient, String subject, String body) {
        return new EmailOutbox(fromAddress, recipient, subject, body);
    }

    public void markSent(Instant now) {
        status = "SENT";
        sentAt = now;
        lastError = null;
    }

    public void markFailed(Instant now, int maxAttempts, java.time.Duration retryDelay, String error) {
        attempts++;
        lastError = error == null ? null : error.substring(0, Math.min(error.length(), 1000));
        if (attempts >= maxAttempts) {
            status = "FAILED";
        } else {
            status = "PENDING";
            nextAttemptAt = now.plus(retryDelay.multipliedBy(attempts));
        }
    }

    public Long getId() { return id; }
    public String getFromAddress() { return fromAddress; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
}
