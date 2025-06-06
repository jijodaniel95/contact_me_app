package com.contactme.contact_me_app.dto;

public class RateLimitResponse {
    private boolean rateLimited;
    private String message;
    private Long retryAfterSeconds; // Time until next attempt is allowed, in seconds

    public RateLimitResponse(boolean rateLimited, String message, Long retryAfterSeconds) {
        this.rateLimited = rateLimited;
        this.message = message;
        this.retryAfterSeconds = retryAfterSeconds;
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
}