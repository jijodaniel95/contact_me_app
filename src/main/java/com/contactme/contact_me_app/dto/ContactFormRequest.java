package com.contactme.contact_me_app.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Transfer Object (DTO) for contact form requests.
 * This class holds the data submitted by users through the contact form.
 * Logging in DTOs is typically minimal, primarily for debugging instantiation
 * or data validation if it were to occur within the DTO itself.
 */
public class ContactFormRequest {

    // Initialize a logger for this class.
    // In DTOs, loggers are less frequently used in getters/setters,
    // but can be useful in constructors or for debugging the object's state.
    private static final Logger logger = LoggerFactory.getLogger(ContactFormRequest.class);

    private String fullName;
    private String email;
    private String subject;
    private String messageText;

    /**
     * Default constructor for ContactFormRequest.
     * Logs the creation of a new instance for debugging purposes.
     */
    public ContactFormRequest() {
        logger.debug("New ContactFormRequest instance created.");
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    // It's often useful to override toString() in DTOs for better logging of object state.
    // Uncomment and implement if desired:
    // @Override
    // public String toString() {
    //     return "ContactFormRequest{" +
    //            "fullName='" + fullName + '\'' +
    //            ", email='" + email + '\'' +
    //            ", subject='" + subject + '\'' +
    //            ", messageText length=" + (messageText != null ? messageText.length() : 0) +
    //            '}';
    // }
}