package com.contactme.contact_me_app.service;


import com.contactme.contact_me_app.dto.ContactFormRequest;
import com.contactme.contact_me_app.entity.ContactFormSubmission;
import com.contactme.contact_me_app.exception.UnexpectedException;
import com.contactme.contact_me_app.repository.ContactFormRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

/**
 * Service class responsible for handling business logic related to contact form submissions.
 * This includes saving submissions to the database.
 */
@Service
public class ContactFormService {

    // Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(ContactFormService.class);

    private final ContactFormRepository contactFormRepository;

    /**
     * Constructor for ContactFormService.
     * @param contactFormRepository The repository for interacting with contact form submission data.
     */
    public ContactFormService(ContactFormRepository contactFormRepository) {
        this.contactFormRepository = contactFormRepository;
        logger.info("ContactFormService initialized.");
    }

    /**
     * Saves a contact form request to the database.
     * It maps the DTO to an entity and persists it.
     *
     * @param request The ContactFormRequest DTO containing the submission details.
     * @return The saved ContactFormSubmission entity with generated ID.
     */
    public ContactFormSubmission saveSubmission(ContactFormRequest request) {
        logger.info("Attempting to save contact form submission for email: {}", request.getEmail());
        logger.debug("Mapping ContactFormRequest to ContactFormSubmission entity.");

        try {
            ContactFormSubmission newSubmission = new ContactFormSubmission();
            newSubmission.setFullName(request.getFullName());
            newSubmission.setEmail(request.getEmail());
            newSubmission.setSubject(request.getSubject());
            newSubmission.setMessageText(request.getMessageText());

            logger.debug("Saving new submission to repository: {}", newSubmission.getEmail());
            ContactFormSubmission savedSubmission = contactFormRepository.save(newSubmission);
            logger.info("Contact form submission saved successfully with ID: {}", savedSubmission.getId());
            return savedSubmission;
        } catch (DataAccessException e) {
            // This block will catch exceptions related to database operations,
            // including those that wrap SQLException (e.g., constraint violations, connection issues).
            logger.error("Database persistence error encountered while saving contact form for email {}: {}",
                         request.getEmail(), e.getMessage(), e);
            // Re-throw as your custom UnexpectedException or a more specific service exception
            throw new UnexpectedException("A database error occurred: " + e.getMessage(), e);
        } catch (Exception e) {
            // This block will catch any other runtime exceptions that are not DataAccessExceptions.
            logger.error("An unexpected error occurred while saving contact form for email {}: {}",
                         request.getEmail(), e.getMessage(), e);
            // Re-throw as your generic UnexpectedException
            throw new UnexpectedException("An unexpected error occurred: " + e.getMessage(), e);
        }
    }
}