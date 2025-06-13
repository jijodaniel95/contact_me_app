package com.contactme.contact_me_app.controller;

import com.contactme.contact_me_app.dto.ContactFormRequest;
import com.contactme.contact_me_app.entity.ContactFormSubmission;
import com.contactme.contact_me_app.kafka.KafkaProducer;
import com.contactme.contact_me_app.service.ContactFormService;
import com.contactme.contact_me_app.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Controller for handling contact form submissions.
 * It manages the endpoint for receiving contact requests,
 * interacting with rate limiting and contact form services.
 */
@RestController
@RequestMapping("/api")
public class ContactFormController {

    // Initialize a logger for this class.
    // SLF4J provides a simple facade for logging, allowing you to plug in
    // different logging implementations (like Logback or Log4j2) at runtime.
    private static final Logger logger = LoggerFactory.getLogger(ContactFormController.class);

    private final RateLimitService rateLimitService;
    private final ContactFormService contactFormService;
    private  final KafkaProducer kafkaProducer;

    /**
     * Constructor for ContactFormController.
     * @param rateLimitService Service to handle rate limiting logic.
     * @param contactFormService Service to handle contact form submission persistence.
     */
    public ContactFormController(RateLimitService rateLimitService, ContactFormService contactFormService,KafkaProducer kafkaProducer) {
        this.rateLimitService = rateLimitService;
        this.contactFormService = contactFormService;
        this.kafkaProducer = kafkaProducer;
        logger.info("ContactFormController initialized with RateLimitService and ContactFormService.");
    }

    /**
     * Handles POST requests for contact form submissions.
     * This endpoint receives contact details, saves them, and records the submission for rate limiting.
     *
     * @param request The ContactFormRequest DTO containing submission details.
     * @param httpRequest The HttpServletRequest to get client IP address.
     * @return ResponseEntity indicating success or failure of the submission.
     */
    @PostMapping("/contact")
    public ResponseEntity<String> submitContactForm(@RequestBody ContactFormRequest request, HttpServletRequest httpRequest) {
        // Log the initiation of the submission process
        logger.info("Attempting to submit contact form from IP: {}", httpRequest.getRemoteAddr());
        logger.debug("Request details: Full Name={}, Email={}, Subject={}, MessageLength={}",
                request.getFullName(), request.getEmail(), request.getSubject(), request.getMessageText().length());

        try {
            // The RateLimitInterceptor has already handled blocking rate-limited requests.
            String ipAddress = httpRequest.getRemoteAddr();

            // Save the contact form submission details using the service
            ContactFormSubmission contactFormSubmission = contactFormService.saveSubmission(request);
            logger.info("Contact form submission saved successfully. Submission ID: {}", contactFormSubmission.getId());

            // Record the successful submission for rate limiting purposes in 'contact_form_submissions_ip' table
            rateLimitService.recordSubmission(ipAddress);
            logger.debug("Recorded successful submission for IP: {}", ipAddress);

            // Existing System.out.println statements are replaced with logger statements
            // This ensures all output goes through the configured logging framework (e.g., Logback)
            logger.info("Received contact form submission from IP: {}", ipAddress);
            logger.info("Full Name: {}", request.getFullName());
            logger.info("Email: {}", request.getEmail());
            logger.info("Subject: {}", request.getSubject());
            logger.info("Message: {}", request.getMessageText());
            logger.info("Submission ID: {}", contactFormSubmission.getId());
            this.kafkaProducer.sendMessage(contactFormSubmission.getId());
            // Return a success response
            return new ResponseEntity<>("Contact form submitted successfully!", HttpStatus.OK);
        } catch (Exception e) {
            // Log any unexpected errors during the submission process
            logger.error("Error submitting contact form for IP {}: {}", httpRequest.getRemoteAddr(), e.getMessage(), e);
            // Return an internal server error response
            return new ResponseEntity<>("Failed to submit contact form.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}