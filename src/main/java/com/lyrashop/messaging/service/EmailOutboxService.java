package com.lyrashop.messaging.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.messaging.entity.EmailOutbox;
import com.lyrashop.messaging.repository.EmailOutboxRepository;

@Service
public class EmailOutboxService {
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration RETRY_DELAY = Duration.ofMinutes(2);

    private final EmailOutboxRepository outbox;
    private final JavaMailSender mailSender;

    public EmailOutboxService(EmailOutboxRepository outbox, JavaMailSender mailSender) {
        this.outbox = outbox;
        this.mailSender = mailSender;
    }

    @Transactional
    public void enqueue(String from, String recipient, String subject, String body) {
        outbox.save(EmailOutbox.create(from, recipient, subject, body));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliver(Long id) {
        EmailOutbox email = outbox.findPendingForUpdate(id).orElse(null);
        if (email == null) return;
        Instant now = Instant.now();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email.getFromAddress());
        message.setTo(email.getRecipient());
        message.setSubject(email.getSubject());
        message.setText(email.getBody());
        try {
            mailSender.send(message);
            email.markSent(now);
        } catch (MailException exception) {
            email.markFailed(now, MAX_ATTEMPTS, RETRY_DELAY, exception.getMessage());
        }
        outbox.save(email);
    }
}
