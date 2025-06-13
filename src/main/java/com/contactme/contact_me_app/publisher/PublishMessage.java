package com.contactme.contact_me_app.publisher;

import com.contactme.contact_me_app.dto.NotificationMessage;

public interface PublishMessage {
    public void sendMessage(NotificationMessage message);
}
