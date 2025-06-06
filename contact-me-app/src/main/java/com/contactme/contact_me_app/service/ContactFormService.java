package com.contactme.contact_me_app.service;


import com.contactme.contact_me_app.dto.ContactFormRequest;
import com.contactme.contact_me_app.entity.ContactFormSubmission;
import com.contactme.contact_me_app.repository.ContactFormRepository;
import org.springframework.stereotype.Service;

@Service
public class ContactFormService {

    private final ContactFormRepository contactFormRepository;

    public ContactFormService(ContactFormRepository contactFormRepository) {
        this.contactFormRepository = contactFormRepository;
    }

    public ContactFormSubmission saveSubmission(ContactFormRequest request) {
        ContactFormSubmission newSubmission = new ContactFormSubmission();
        newSubmission.setFullName(request.getFullName());
        newSubmission.setEmail(request.getEmail());
        newSubmission.setSubject(request.getSubject());
        newSubmission.setMessageText(request.getMessageText());
        return contactFormRepository.save(newSubmission);
    }
}