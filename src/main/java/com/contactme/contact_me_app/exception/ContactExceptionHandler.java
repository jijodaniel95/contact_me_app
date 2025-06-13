package com.contactme.contact_me_app.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.HashMap;
import java.util.Map;

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
     * This is typically for cases where an unexpected null value caused an internal error.
     * It returns an HTTP 500 Internal Server Error.
     *
     * @param ex The NullPointerException that occurred.
     * @return A ResponseEntity with HTTP status 500 and a generic error message.
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException ex) {
        logger.error("NullPointerException caught by handler: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("An internal server error occurred due to a missing value.");
    }

    /**
     * Handles MethodArgumentNotValidException.
     * This exception is thrown when @Valid annotation fails on request body/parameters.
     * It returns an HTTP 400 Bad Request with details about validation errors.
     *
     * @param ex The MethodArgumentNotValidException that occurred.
     * @return A ResponseEntity with HTTP status 400 and a map of field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        logger.warn("Validation errors occurred: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles HttpMessageNotReadableException.
     * This exception is thrown when the request body cannot be read or parsed (e.g., malformed JSON).
     * It returns an HTTP 400 Bad Request.
     *
     * @param ex The HttpMessageNotReadableException that occurred.
     * @return A ResponseEntity with HTTP status 400 and a generic error message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("HttpMessageNotReadableException caught by handler: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed JSON request body.");
    }

    /**
     * Handles UnexpectedException.
     * This is a custom exception, likely for unexpected server-side issues originating from services.
     *
     * @param ex The UnexpectedException that occurred.
     * @return A ResponseEntity with HTTP status 500 and the exception message.
     */
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<String> handleUnexpectedException(UnexpectedException ex){
        logger.error("UnexpectedException caught by handler: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    // You might also consider adding a generic Exception handler
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<String> handleGenericException(Exception ex) {
    //     logger.error("An unhandled exception occurred: {}", ex.getMessage(), ex);
    //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    // }
}
