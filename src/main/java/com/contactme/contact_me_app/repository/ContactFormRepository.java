package com.contactme.contact_me_app.repository;

import com.contactme.contact_me_app.entity.ContactFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface ContactFormRepository extends JpaRepository<ContactFormSubmission, Long> {

    /**
     * Custom method to delete all contact form entries with sent_at before a given timestamp.
     * Returns the number of deleted records.
     */
    int deleteByIsReadTrue(); // Add this line
}