package com.contactme.contact_me_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;import java.time.OffsetDateTime;

@Entity
@Table(name = "contact_form_submissions")
public class ContactFormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "subject")
    private String subject;

    @Column(name = "message_text", nullable = false)
    private String messageText;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "notification_retry_count")
    private Integer notificationRetryCount;

    @Column(name = "last_notification_attempt_at")
    private OffsetDateTime lastNotificationAttemptAt;

    public ContactFormSubmission() {
        // Set default values as defined in your SQL schema
        this.sentAt = OffsetDateTime.now();
        this.isRead = false;
        this.notificationRetryCount = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public Integer getNotificationRetryCount() { return notificationRetryCount; }
    public void setNotificationRetryCount(Integer notificationRetryCount) { this.notificationRetryCount = notificationRetryCount; }
    public OffsetDateTime getLastNotificationAttemptAt() { return lastNotificationAttemptAt; }
    public void setLastNotificationAttemptAt(OffsetDateTime lastNotificationAttemptAt) { this.lastNotificationAttemptAt = lastNotificationAttemptAt; }
}