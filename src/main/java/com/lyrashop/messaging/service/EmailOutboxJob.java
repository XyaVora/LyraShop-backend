package com.lyrashop.messaging.service;

import java.time.Instant;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lyrashop.messaging.repository.EmailOutboxRepository;

@Component
public class EmailOutboxJob {
    private final EmailOutboxRepository outbox;
    private final EmailOutboxService delivery;

    public EmailOutboxJob(EmailOutboxRepository outbox, EmailOutboxService delivery) {
        this.outbox = outbox;
        this.delivery = delivery;
    }

    @Scheduled(fixedDelayString = "${app.mail-outbox.sweep-delay:30s}", initialDelayString = "${app.mail-outbox.initial-delay:5s}")
    public void sendDueEmails() {
        outbox.findDueIds(Instant.now(), PageRequest.of(0, 50)).forEach(delivery::deliver);
    }
}
