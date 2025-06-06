package com.contactme.contact_me_app.service;

import com.contactme.contact_me_app.repository.ContactFormIpSubmissionRepository;
import com.contactme.contact_me_app.repository.ContactFormRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class RateLimitCleanupService {

    private final ContactFormIpSubmissionRepository ipRepository; // Renamed for clarity
    private final ContactFormRepository contactFormRepository; // New injection

    // Configure how old entries must be before they are deleted
    @Value("${data-cleanup.days-old:20}") // Renamed property prefix for general data cleanup
    private int daysOldToKeep;

    public RateLimitCleanupService(
            ContactFormIpSubmissionRepository ipRepository,
            ContactFormRepository contactFormRepository // Inject both repositories
    ) {
        this.ipRepository = ipRepository;
        this.contactFormRepository = contactFormRepository;
    }

    /**
     * Scheduled task to clean up old rate limit entries and old contact form submissions.
     * Runs every 24 hours (at midnight).
     */
    @Scheduled(cron = "0 0 0 * * *") // This means 00:00:00 (midnight) every day
    @Transactional // Ensure the delete operations are atomic
    public void cleanupOldData() { // Renamed method to be more general
        OffsetDateTime cleanupThreshold = OffsetDateTime.now().minus(Duration.ofDays(daysOldToKeep));

        // 1. Clean up old rate limit entries
        int ipDeletedCount = ipRepository.deleteBySubmissionTimeBefore(cleanupThreshold);
        System.out.println("Scheduler: Cleaned up " + ipDeletedCount + " rate limit entries older than " + daysOldToKeep + " days.");

        // 2. Clean up old actual contact form submissions
        // IMPORTANT: This deletes actual user messages. Be sure this is intended.x
        int contactFormDeletedCount = contactFormRepository.deleteByIsReadTrue();
        System.out.println("Scheduler: Cleaned up " + contactFormDeletedCount + " contact form submissions older than " + daysOldToKeep + " days.");
    }
}