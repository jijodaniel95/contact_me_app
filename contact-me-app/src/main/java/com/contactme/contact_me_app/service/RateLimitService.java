package com.contactme.contact_me_app.service;


import com.contactme.contact_me_app.dto.RateLimitResponse;
import com.contactme.contact_me_app.entity.ContactFormIpSubmission;
import com.contactme.contact_me_app.repository.ContactFormIpSubmissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional; // Used to check for the *latest* submission

@Service
public class RateLimitService {
    private final ContactFormIpSubmissionRepository repository;

    @Value("${rate-limit.max-overall-requests:3}")
    private int maxOverallRequests;

    // Default changed to 1440 (24 hours) to explicitly represent "per day" in the code as well
    @Value("${rate-limit.overall-window-minutes:1440}")
    private int overallWindowMinutes;

    @Value("${rate-limit.cooldown-minutes:5}")
    private int cooldownMinutes;

    public RateLimitService(ContactFormIpSubmissionRepository repository) {
        this.repository = repository;
    }

    public RateLimitResponse checkRateLimit(String ipAddress) {
        OffsetDateTime now = OffsetDateTime.now();

        // Rule 1: Check against the total maximum requests per day
        OffsetDateTime overallWindowStart = now.minus(Duration.ofMinutes(overallWindowMinutes));
        List<ContactFormIpSubmission> allRecentSubmissions = repository.findByIpAddressAndSubmissionTimeAfter(ipAddress, overallWindowStart);

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
            if (retryAfterSeconds < 0) retryAfterSeconds = 0L; // Should not be negative

            System.out.println("Rate Limited (Max Overall Requests per Day) for IP: " + ipAddress + " - Count: " + allRecentSubmissions.size() + ", Retry in: " + retryAfterSeconds + "s");
            return new RateLimitResponse(true, "You have exceeded the daily submission limit.", retryAfterSeconds);
        }

        // Rule 2: Check the cooldown period from the *latest* submission
        Optional<ContactFormIpSubmission> latestSubmissionOptional = allRecentSubmissions.stream()
                .max((s1, s2) -> s1.getSubmissionTime().compareTo(s2.getSubmissionTime()));

        if (latestSubmissionOptional.isPresent()) {
            OffsetDateTime lastSubmissionTime = latestSubmissionOptional.get().getSubmissionTime();
            OffsetDateTime nextAllowedSubmissionTime = lastSubmissionTime.plus(Duration.ofMinutes(cooldownMinutes));

            if (now.isBefore(nextAllowedSubmissionTime)) {
                Long retryAfterSeconds = Duration.between(now, nextAllowedSubmissionTime).getSeconds();
                System.out.println("Rate Limited (Cooldown) for IP: " + ipAddress + " - Last submission: " + lastSubmissionTime + ", Retry in: " + retryAfterSeconds + "s");
                return new RateLimitResponse(true, "Please wait before sending another message.", retryAfterSeconds);
            }
        }

        System.out.println("Not Rate Limited for IP: " + ipAddress);
        return new RateLimitResponse(false, "Allowed", null);
    }

    public void recordSubmission(String ipAddress) {
        ContactFormIpSubmission newSubmission = new ContactFormIpSubmission(ipAddress, OffsetDateTime.now());
        repository.save(newSubmission);
        System.out.println("Recorded submission for IP: " + ipAddress);
    }
}