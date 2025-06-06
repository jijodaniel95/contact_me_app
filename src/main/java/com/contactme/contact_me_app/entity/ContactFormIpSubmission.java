package com.contactme.contact_me_app.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "contact_form_submissions_ip")
public class ContactFormIpSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "submission_time", nullable = false)
    private OffsetDateTime submissionTime;

    public ContactFormIpSubmission() {
    }

    public ContactFormIpSubmission(String ipAddress, OffsetDateTime submissionTime) {
        this.ipAddress = ipAddress;
        this.submissionTime = submissionTime;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public OffsetDateTime getSubmissionTime() { return submissionTime; }
    public void setSubmissionTime(OffsetDateTime submissionTime) { this.submissionTime = submissionTime; }
}
