package com.contactme.contact_me_app.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Transfer Object (DTO) for conveying rate limit check results.
 * This class indicates whether a request is rate-limited, provides a message,
 * and suggests a retry time if applicable.
 * Logging in DTOs is usually minimal, focused on constructor calls to see the object's state.
 */
public class RateLimitResponse {

    // Initialize a logger for this class.
    // Useful for debugging when instances of this DTO are created.
    private static final Logger logger = LoggerFactory.getLogger(RateLimitResponse.class);

    private boolean rateLimited;
    private String message;
    private Long retryAfterSeconds; // Time until next attempt is allowed, in seconds

    /**
     * Constructor for RateLimitResponse.
     * @param rateLimited True if the request is rate-limited, false otherwise.
     * @param message A descriptive message about the rate limit status.
     * @param retryAfterSeconds The number of seconds to wait before retrying, or null/0 if not applicable.
     */
    public RateLimitResponse(boolean rateLimited, String message, Long retryAfterSeconds) {
        this.rateLimited = rateLimited;
        this.message = message;
        this.retryAfterSeconds = retryAfterSeconds;
        // Log the state of the RateLimitResponse when it's created for debugging
        logger.debug("New RateLimitResponse created: rateLimited={}, message='{}', retryAfterSeconds={}",
                     rateLimited, message, retryAfterSeconds);
    }

    public boolean isRateLimited() {
        return rateLimited;
    }

    public String getMessage() {
        return message;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    // It's often useful to override toString() in DTOs for better logging of object state.
    // Uncomment and implement if desired:
    // @Override
    // public String toString() {
    //     return "RateLimitResponse{" +
    //            "rateLimited=" + rateLimited +
    //            ", message='" + message + '\'' +
    //            ", retryAfterSeconds=" + retryAfterSeconds +
    //            '}';
    // }
}