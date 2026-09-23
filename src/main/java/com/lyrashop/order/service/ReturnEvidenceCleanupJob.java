package com.lyrashop.order.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReturnEvidenceCleanupJob {
    private final ReturnEvidenceUploadService uploads;
    public ReturnEvidenceCleanupJob(ReturnEvidenceUploadService uploads){this.uploads=uploads;}

    @Scheduled(fixedDelayString="${app.uploads.cleanup-delay:10m}",initialDelayString="${app.uploads.cleanup-initial-delay:1m}")
    public void cleanup(){while(uploads.cleanupExpired()==100){}}
}
