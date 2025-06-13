package com.contactme.contact_me_app.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global exception handler for the application.
 * This class uses @ControllerAdvice to provide centralized exception handling
 * across all @Controller classes, ensuring a consistent error response.
 */
@ControllerAdvice
public class ContactExceptionHandler {

    // Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(ContactExceptionHandler.class);

    /**
     * Default constructor for ContactExceptionHandler.
     */
    public ContactExceptionHandler() {
        logger.info("ContactExceptionHandler initialized, ready to handle exceptions.");
    }

    /**
     * Handles NullPointerException.
     * This is typically for cases where an expected object was null.
     *
     * @param ex The NullPointerException that occurred.
     * @return A string indicating the view name for an error page.
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleContactNotFoundException(NullPointerException ex) {
        // Log the exception at ERROR level with its stack trace for debugging
        logger.error("NullPointerException caught by handler: {}", ex.getMessage(), ex);
        // Returning a view name, so this is likely for server-side rendering
        return ResponseEntity.status(500).body(ex.getMessage());
    }

    /**
     * Handles UnexpectedException.
     * This is a custom exception, likely for unexpected server-side issues.
     *
     * @param ex The UnexpectedException that occurred.
     * @return A ResponseEntity with HTTP status 500 and the exception message.
     */
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<String> handleUnexpectedException(UnexpectedException ex){
        // Log the custom UnexpectedException at ERROR level with its stack trace
        logger.error("UnexpectedException caught by handler: {}", ex.getMessage(), ex);
        // Returning a ResponseEntity with an error message and 500 status
        return ResponseEntity.status(500).body(ex.getMessage());
    }

    // You might also consider adding a generic Exception handler
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<String> handleGenericException(Exception ex) {
    //     logger.error("An unhandled exception occurred: {}", ex.getMessage(), ex);
    //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    // }
}
