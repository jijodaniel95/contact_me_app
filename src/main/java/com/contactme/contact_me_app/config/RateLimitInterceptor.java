package com.contactme.contact_me_app.config;// src/main/java/com/example/contactform/config/RateLimitInterceptor.java


import com.contactme.contact_me_app.dto.RateLimitResponse;
import com.contactme.contact_me_app.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper; // For converting response to JSON

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration; // Import Duration

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String ipAddress = request.getRemoteAddr();

        RateLimitResponse rateLimitCheck = rateLimitService.checkRateLimit(ipAddress);

        if (rateLimitCheck.isRateLimited()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            // Standard HTTP header
            response.setHeader("Retry-After", String.valueOf(rateLimitCheck.getRetryAfterSeconds()));

            // --- Construct the combined message here ---
            String baseMessage = rateLimitCheck.getMessage();
            Long retryAfterSeconds = rateLimitCheck.getRetryAfterSeconds();

            String fullResponseMessage;

            // Only append specific retry time if it's a positive value
            if (retryAfterSeconds != null && retryAfterSeconds > 0) {
                Duration duration = Duration.ofSeconds(retryAfterSeconds);
                long minutes = duration.toMinutes();
                long seconds = duration.toSecondsPart(); // Remaining seconds after extracting minutes

                StringBuilder retryMessageBuilder = new StringBuilder("Please try again in ");

                if (minutes > 0 && seconds > 0) {
                    // Example: "1 minute and 30 seconds"
                    retryMessageBuilder.append(minutes).append(" minute");
                    if (minutes > 1) retryMessageBuilder.append("s");
                    retryMessageBuilder.append(" and ").append(seconds).append(" second");
                    if (seconds > 1) retryMessageBuilder.append("s");
                } else if (minutes > 0) {
                    // Example: "2 minutes"
                    retryMessageBuilder.append(minutes).append(" minute");
                    if (minutes > 1) retryMessageBuilder.append("s");
                } else if (seconds > 0) {
                    // Example: "30 seconds"
                    retryMessageBuilder.append(seconds).append(" second");
                    if (seconds > 1) retryMessageBuilder.append("s");
                } else {
                    // This covers cases where retryAfterSeconds is positive but very small (e.g., < 1s)
                    // or due to precision issues it becomes 0 minutes and 0 seconds
                    retryMessageBuilder.append("a moment.");
                }
                retryMessageBuilder.append("."); // Add period at the end

                fullResponseMessage = baseMessage + " " + retryMessageBuilder.toString();
            } else {
                // If retryAfterSeconds is 0, null, or negative, just provide the base message
                fullResponseMessage = baseMessage + " Please try again later.";
            }

            // Prepare a JSON response body
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", fullResponseMessage);

            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

            return false;
        }
        return true;
    }
}