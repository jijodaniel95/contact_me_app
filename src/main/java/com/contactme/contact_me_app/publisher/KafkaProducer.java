package com.contactme.contact_me_app.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * KafkaProducer is responsible for asynchronously sending messages to a Kafka topic.
 * It uses Spring's KafkaTemplate to interact with the Kafka broker.
 */
@Service
public class KafkaProducer implements PublishMessage {

    // Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, Long> kafkaTemplate;
    private final String topic = "contact_me_topic";

    /**
     * Constructor for KafkaProducer.
     * @param kafkaTemplate The Spring KafkaTemplate instance for sending messages.
     */
    public KafkaProducer(KafkaTemplate<String, Long> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        logger.info("KafkaProducer initialized with topic: {}", topic);
    }
    /**
     * Asynchronously sends a message (Long ID) to the configured Kafka topic.
     * Logs the attempt, success, or failure of sending the message.
     * @param message The Long value (e.g., a submission ID) to send.
     */
    @Async
    @Override
    public void sendMessage(Long message) {

        logger.debug("Attempting to send message to topic '{}': {}", topic, message);
        try {
            kafkaTemplate.send(topic, message);
            logger.info("Successfully sent message '{}' to Kafka topic '{}'.", message, topic);
        } catch (Exception e) {
            // Log any errors that occur during the message sending process
            logger.error("Failed to send message '{}' to Kafka topic '{}': {}", message, topic, e.getMessage(), e);
        }
    }



}
