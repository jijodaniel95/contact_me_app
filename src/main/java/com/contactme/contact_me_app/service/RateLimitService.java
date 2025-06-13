package com.contactme.contact_me_app.service;


import com.contactme.contact_me_app.dto.RateLimitResponse;
import com.contactme.contact_me_app.entity.ContactFormIpSubmission;
import com.contactme.contact_me_app.repository.ContactFormIpSubmissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional; // Used to check for the *latest* submission

/**
 * Service class responsible for implementing and checking rate limiting logic
 * for incoming contact form submissions based on IP address.
 */
@Service
public class RateLimitService {

    // Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    private final ContactFormIpSubmissionRepository repository;

    @Value("${rate-limit.max-overall-requests:3}")
    private int maxOverallRequests;

    // Default changed to 1440 (24 hours) to explicitly represent "per day" in the code as well
    @Value("${rate-limit.overall-window-minutes:1440}")
    private int overallWindowMinutes;

    @Value("${rate-limit.cooldown-minutes:5}")
    private int cooldownMinutes;

    /**
     * Constructor for RateLimitService.
     * @param repository The repository for IP submission records.
     */
    public RateLimitService(ContactFormIpSubmissionRepository repository) {
        this.repository = repository;
        logger.info("RateLimitService initialized. Max Overall Requests: {}, Overall Window: {} minutes, Cooldown: {} minutes.",
                    maxOverallRequests, overallWindowMinutes, cooldownMinutes);
    }

    /**
     * Checks if a given IP address is currently rate-limited based on two rules:
     * 1. Maximum overall requests within a defined window.
     * 2. Cooldown period after the latest submission.
     *
     * @param ipAddress The IP address to check.
     * @return A RateLimitResponse indicating if the IP is limited, with a message and retry time.
     */
    public RateLimitResponse checkRateLimit(String ipAddress) {
        logger.debug("Checking rate limit for IP: {}", ipAddress);
        OffsetDateTime now = OffsetDateTime.now();

        // Rule 1: Check against the total maximum requests per day
        OffsetDateTime overallWindowStart = now.minus(Duration.ofMinutes(overallWindowMinutes));
        logger.debug("Overall window starts at: {}", overallWindowStart);
        List<ContactFormIpSubmission> allRecentSubmissions = repository.findByIpAddressAndSubmissionTimeAfter(ipAddress, overallWindowStart);
        logger.debug("Found {} submissions for IP {} within the overall window.", allRecentSubmissions.size(), ipAddress);

        if (allRecentSubmissions.size() >= maxOverallRequests) {
            // If overall limit hit, calculate when the *earliest* submission in the window will expire
            // This is complex for a "fixed window" as it's the *window itself* that resets.
            // For simplicity, let's say retry after the window ends, or a fixed large amount.
            // A more accurate "next retry" for this rule would be for the *oldest* relevant submission
            // to pass out of the window.
            OffsetDateTime oldestRelevantSubmissionTime = allRecentSubmissions.stream()
                    .min((s1, s2) -> s1.getSubmissionTime().compareTo(s2.getSubmissionTime()))
                    .orElse(null)
                    .getSubmissionTime(); // Will not be null if size >= maxOverallRequests

            OffsetDateTime windowEnds = oldestRelevantSubmissionTime.plus(Duration.ofMinutes(overallWindowMinutes));
            Long retryAfterSeconds = Duration.between(now, windowEnds).getSeconds();
            if (retryAfterSeconds < 0) { // Should not be negative, but defensive check
                retryAfterSeconds = 0L;
                logger.warn("Calculated retryAfterSeconds was negative for IP {}. Resetting to 0.", ipAddress);
            }

            logger.warn("Rate Limited (Max Overall Requests per Day) for IP: {}. Count: {}. Retry in: {}s.",
                    ipAddress, allRecentSubmissions.size(), retryAfterSeconds);
            return new RateLimitResponse(true, "You have exceeded the daily submission limit.", retryAfterSeconds);
        }

        // Rule 2: Check the cooldown period from the *latest* submission
        Optional<ContactFormIpSubmission> latestSubmissionOptional = allRecentSubmissions.stream()
                .max((s1, s2) -> s1.getSubmissionTime().compareTo(s2.getSubmissionTime()));

        if (latestSubmissionOptional.isPresent()) {
            OffsetDateTime lastSubmissionTime = latestSubmissionOptional.get().getSubmissionTime();
            OffsetDateTime nextAllowedSubmissionTime = lastSubmissionTime.plus(Duration.ofMinutes(cooldownMinutes));
            logger.debug("Latest submission for IP {}: {}. Next allowed submission: {}", ipAddress, lastSubmissionTime, nextAllowedSubmissionTime);

            if (now.isBefore(nextAllowedSubmissionTime)) {
                Long retryAfterSeconds = Duration.between(now, nextAllowedSubmissionTime).getSeconds();
                logger.warn("Rate Limited (Cooldown) for IP: {}. Last submission: {}. Retry in: {}s.",
                        ipAddress, lastSubmissionTime, retryAfterSeconds);
                return new RateLimitResponse(true, "Please wait before sending another message.", retryAfterSeconds);
            }
        }

        logger.debug("IP {} is not rate-limited. Allowing request.", ipAddress);
        return new RateLimitResponse(false, "Allowed", null);
    }

    /**
     * Records a new submission for the given IP address.
     *
     * @param ipAddress The IP address for which to record the submission.
     */
    public void recordSubmission(String ipAddress) {
        logger.info("Recording submission for IP: {}", ipAddress);
        ContactFormIpSubmission newSubmission = new ContactFormIpSubmission(ipAddress, OffsetDateTime.now());
        repository.save(newSubmission);
        logger.debug("Successfully recorded submission for IP: {}", ipAddress);
    }
}