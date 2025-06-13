package com.contactme.contact_me_app.publisher;

import com.contactme.contact_me_app.dto.NotificationMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.kafka.KafkaException;
import com.contactme.contact_me_app.exception.UnexpectedException;

/**
 * KafkaProducer is responsible for asynchronously sending messages to a Kafka topic.
 * It uses Spring's KafkaTemplate to interact with the Kafka broker.
 */
@Service
public class KafkaProducer implements PublishMessage {

    // Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;
    private final String topic = "contact_me_topic";

    /**
     * Constructor for KafkaProducer.
     * @param kafkaTemplate The Spring KafkaTemplate instance for sending messages.
     */
    public KafkaProducer(KafkaTemplate<String, NotificationMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        logger.info("KafkaProducer initialized with topic: {}", topic);
    }

    /**
     * Asynchronously sends a NotificationMessage object to the configured Kafka topic.
     * Logs the attempt, success, or failure of sending the message.
     *
     * @param message The NotificationMessage object to send.
     * @throws UnexpectedException if an error occurs during Kafka message production.
     */
    @Async
    @Override
    public void sendMessage(NotificationMessage message) {
        // No longer extracting a Long ID, sending the full message object.
        // You might want a unique key for the message, e.g., message.getSomeId().toString() as the key.
        // For simplicity, sending with null key for now.

        logger.debug("Attempting to send message to topic '{}'. Message: {}", topic, message);
        try {
            // Sending the entire NotificationMessage object
            kafkaTemplate.send(topic, message);
            logger.info("Successfully sent message to Kafka topic '{}': {}", topic, message);
        } catch (KafkaException e) { // Catch specific Kafka exceptions
            // Log errors that occur during the message sending process via KafkaTemplate
            logger.error("Failed to send message '{}' to Kafka topic '{}': {}", message, topic, e.getMessage(), e);
            throw new UnexpectedException("Failed to publish message to Kafka.", e);
        } catch (Exception e) { // General catch for any other unforeseen exceptions
            logger.error("An unexpected error occurred while sending message '{}' to Kafka topic '{}': {}",
                         message, topic, e.getMessage(), e);
            throw new UnexpectedException("An unknown error occurred during Kafka publish.", e);
        }
    }
}
