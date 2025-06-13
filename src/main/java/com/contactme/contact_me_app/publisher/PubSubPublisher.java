package com.contactme.contact_me_app.publisher;


import com.contactme.contact_me_app.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.contactme.contact_me_app.exception.UnexpectedException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Service class responsible for publishing messages to Google Cloud Pub/Sub.
 * It implements the PublishMessage interface and uses PubSubTemplate for interaction.
 */
@Service
public class PubSubPublisher implements PublishMessage {

    private static final Logger logger = LoggerFactory.getLogger(PubSubPublisher.class);

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public PubSubPublisher(PubSubTemplate pubSubTemplate, ObjectMapper objectMapper) {
        this.pubSubTemplate = pubSubTemplate;
        this.objectMapper = objectMapper;
        this.topic = "contact-me";
        logger.info("PubSubPublisher initialized for default topic: {}", this.topic);
    }

    @Override
    @Async
    public void sendMessage(NotificationMessage message) {
        logger.info("Attempting to publish message to topic '{}'. Message type: {}", topic, message.getClass().getSimpleName());
        try {
            logger.debug("Serializing message to JSON: {}", message);
            String jsonMessage = objectMapper.writeValueAsString(message);
            pubSubTemplate.publish(topic, jsonMessage).get();
            logger.info("Successfully published message to topic '{}': {}", topic, jsonMessage);
        } catch (IOException e) {
            logger.error("Failed to serialize message to JSON for topic '{}': {}", topic, e.getMessage(), e);
            throw new UnexpectedException("Failed to serialize message for Pub/Sub.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Pub/Sub publish interrupted for topic '{}': {}", topic, e.getMessage(), e);
            throw new UnexpectedException("Pub/Sub publish operation was interrupted.", e);
        } catch (ExecutionException e) {
            logger.error("Pub/Sub publish execution failed for topic '{}': {}", topic, e.getMessage(), e);
            throw new UnexpectedException("Pub/Sub publish operation failed.", e);
        } catch (Exception e) {
            logger.error("An unknown error occurred while publishing to topic '{}': {}", topic, e.getMessage(), e);
            throw new UnexpectedException("An unknown error occurred during Pub/Sub publish.", e);
        }
    }
}