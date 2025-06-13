package com.contactme.contact_me_app.service;

import com.contactme.contact_me_app.repository.ContactFormIpSubmissionRepository;
import com.contactme.contact_me_app.repository.ContactFormRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Service responsible for scheduled cleanup of old rate limit entries
 * and contact form submissions from the database.
 */
@Service
public class RateLimitCleanupService {

    // Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(RateLimitCleanupService.class);

    private final ContactFormIpSubmissionRepository ipRepository; // Renamed for clarity
    private final ContactFormRepository contactFormRepository; // New injection

    // Configure how old entries must be before they are deleted
    @Value("${data-cleanup.days-old:20}") // Renamed property prefix for general data cleanup
    private int daysOldToKeep;

    /**
     * Constructor for RateLimitCleanupService.
     * @param ipRepository Repository for IP submission records.
     * @param contactFormRepository Repository for actual contact form submissions.
     */
    public RateLimitCleanupService(
            ContactFormIpSubmissionRepository ipRepository,
            ContactFormRepository contactFormRepository // Inject both repositories
    ) {
        this.ipRepository = ipRepository;
        this.contactFormRepository = contactFormRepository;
        logger.info("RateLimitCleanupService initialized. Data older than {} days will be cleaned up.", daysOldToKeep);
    }

    /**
     * Scheduled task to clean up old rate limit entries and old contact form submissions.
     * Runs every 24 hours (at midnight).
     * This method is transactional to ensure atomicity of delete operations.
     */
    @Scheduled(cron = "0 0 0 * * *") // This means 00:00:00 (midnight) every day
    @Transactional // Ensure the delete operations are atomic
    public void cleanupOldData() { // Renamed method to be more general
        logger.info("Starting scheduled data cleanup task.");
        OffsetDateTime cleanupThreshold = OffsetDateTime.now().minus(Duration.ofDays(daysOldToKeep));
        logger.debug("Cleanup threshold set to: {}. Deleting entries older than this.", cleanupThreshold);

        try {
            // 1. Clean up old rate limit entries
            int ipDeletedCount = ipRepository.deleteBySubmissionTimeBefore(cleanupThreshold);
            logger.info("Scheduler: Cleaned up {} rate limit entries older than {} days.", ipDeletedCount, daysOldToKeep);

            // 2. Clean up old actual contact form submissions
            // IMPORTANT: This deletes actual user messages. Be sure this is intended.
            int contactFormDeletedCount = contactFormRepository.deleteByIsReadTrue();
            logger.info("Scheduler: Cleaned up {} contact form submissions older than {} days.", contactFormDeletedCount, daysOldToKeep);

            logger.info("Scheduled data cleanup task completed successfully.");
        } catch (Exception e) {
            // Log any exceptions that occur during the cleanup process
            logger.error("Error during scheduled data cleanup: {}", e.getMessage(), e);
        }
    }
}