package com.contactme.contact_me_app.repository;

import com.contactme.contact_me_app.entity.ContactFormIpSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ContactFormIpSubmissionRepository extends JpaRepository<ContactFormIpSubmission, Long> {

    /**
     * Finds all IP submissions from a given IP address after a specific time.
     */
    List<ContactFormIpSubmission> findByIpAddressAndSubmissionTimeAfter(String ipAddress, OffsetDateTime time);

    /**
     * Custom method to delete all entries with submission_time before a given timestamp.
     * Returns the number of deleted records.
     */
    int deleteBySubmissionTimeBefore(OffsetDateTime time);
}