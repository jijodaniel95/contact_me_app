package com.contactme.contact_me_app.controller;



import com.contactme.contact_me_app.dto.ContactFormRequest;
import com.contactme.contact_me_app.entity.ContactFormSubmission;
import com.contactme.contact_me_app.service.ContactFormService;
import com.contactme.contact_me_app.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ContactFormController {

    private final RateLimitService rateLimitService;
    private final ContactFormService contactFormService;

    public ContactFormController(RateLimitService rateLimitService, ContactFormService contactFormService) {
        this.rateLimitService = rateLimitService;
        this.contactFormService = contactFormService;
    }

    @PostMapping("/contact")
    public ResponseEntity<String> submitContactForm(@RequestBody ContactFormRequest request, HttpServletRequest httpRequest) {
        // The RateLimitInterceptor has already handled blocking rate-limited requests.
        String ipAddress = httpRequest.getRemoteAddr();
        ContactFormSubmission contactFormSubmission=contactFormService.saveSubmission(request);
        // 2. Record the successful submission for rate limiting purposes in 'contact_form_submissions_ip' table
        rateLimitService.recordSubmission(ipAddress);
        System.out.println("Received contact form submission from IP: " + ipAddress);
        System.out.println("Full Name: " + request.getFullName());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Subject: " + request.getSubject());
        System.out.println("Message: " + request.getMessageText());
        return new ResponseEntity<>("Contact form submitted successfully!", HttpStatus.OK);
    }
}