package com.contactme.contact_me_app.config;// src/main/java/com/example/contactform/config/RateLimitInterceptor.java


import com.contactme.contact_me_app.dto.RateLimitResponse;
import com.contactme.contact_me_app.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper; // For converting response to JSON
import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration; // Import Duration

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for RateLimitInterceptor.
     * @param rateLimitService Service for checking and managing rate limits.
     * @param objectMapper ObjectMapper for converting Java objects to JSON.
     */
    public RateLimitInterceptor(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        logger.info("RateLimitInterceptor initialized.");
    }

    /**
     * Intercepts incoming requests before they are handled by the controller.
     * Checks if the client's IP address is rate-limited.
     * If rate-limited, sets an appropriate HTTP status and response body.
     *
     * @param request The current HttpServletRequest.
     * @param response The current HttpServletResponse.
     * @param handler The handler (Controller method) that will be executed.
     * @return true if the request should proceed, false if it should be blocked.
     * @throws IOException if an I/O error occurs writing the response.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ipAddress = request.getRemoteAddr();
        logger.debug("Pre-handling request from IP: {}", ipAddress);

        RateLimitResponse rateLimitCheck = rateLimitService.checkRateLimit(ipAddress);

        if (rateLimitCheck.isRateLimited()) {
            Long retryAfterSeconds = rateLimitCheck.getRetryAfterSeconds();
            logger.warn("Rate limit exceeded for IP: {}. Message: {}. Retry-After: {} seconds.",
                    ipAddress, rateLimitCheck.getMessage(), retryAfterSeconds);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

            // --- Construct the combined message here ---
            String baseMessage = rateLimitCheck.getMessage();

            String fullResponseMessage;

            // Only append specific retry time if it's a positive value
            if (retryAfterSeconds != null && retryAfterSeconds > 0) {
                Duration duration = Duration.ofSeconds(retryAfterSeconds);
                long minutes = duration.toMinutes();
                long seconds = duration.toSecondsPart(); // Remaining seconds after extracting minutes

                StringBuilder retryMessageBuilder = new StringBuilder("Please try again in ");

                if (minutes > 0 && seconds > 0) {
                    retryMessageBuilder.append(minutes).append(" minute");
                    if (minutes > 1) retryMessageBuilder.append("s");
                    retryMessageBuilder.append(" and ").append(seconds).append(" second");
                    if (seconds > 1) retryMessageBuilder.append("s");
                } else if (minutes > 0) {
                    retryMessageBuilder.append(minutes).append(" minute");
                    if (minutes > 1) retryMessageBuilder.append("s");
                } else if (seconds > 0) {
                    retryMessageBuilder.append(seconds).append(" second");
                    if (seconds > 1) retryMessageBuilder.append("s");
                } else {
                    // This covers cases where retryAfterSeconds is positive but very small (e.g., < 1s)
                    // or due to precision issues it becomes 0 minutes and 0 seconds
                    retryMessageBuilder.append("a moment.");
                }
                retryMessageBuilder.append("."); // Add period at the end

                fullResponseMessage = baseMessage + " " + retryMessageBuilder.toString();
                logger.debug("Generated full retry message: {}", fullResponseMessage);
            } else {
                // If retryAfterSeconds is 0, null, or negative, just provide the base message
                fullResponseMessage = baseMessage + " Please try again later.";
                logger.debug("Generated full retry message (no specific retry time): {}", fullResponseMessage);
            }

            // Prepare a JSON response body
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", fullResponseMessage);

            response.setContentType("application/json");
            try {
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                logger.trace("Wrote rate limit error response for IP: {}", ipAddress);
            } catch (IOException e) {
                logger.error("IOException while writing rate limit response for IP {}: {}", ipAddress, e.getMessage(), e);
            }

            return false; // Block the request
        }
        logger.debug("IP {} is not rate-limited. Proceeding with request.", ipAddress);
        return true; // Allow the request to proceed
    }
}