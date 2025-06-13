# Contact Me Application

A robust backend application designed to handle contact form submissions, featuring rate limiting, asynchronous message processing, and structured logging.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Setup and Installation](#setup-and-installation)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Logging](#logging)
- [Exception Handling](#exception-handling)

## Features

*   **Contact Form Submission**: API endpoint to receive and process contact messages.
*   **Rate Limiting**: Protects the application from abusive traffic by limiting submissions per IP address based on daily limits and a cooldown period.
*   **Asynchronous Messaging**: Utilizes a message broker (Kafka or Google Cloud Pub/Sub) for decoupled processing of submissions, improving responsiveness.
*   **Scheduled Data Cleanup**: Automatically cleans up old rate limit records and contact form submissions to maintain database hygiene.
*   **Structured Logging**: Implemented with SLF4J and Logback for comprehensive, configurable, and efficient logging across all layers of the application.
*   **Global Exception Handling**: Centralized error management for a consistent API response in case of validation, parsing, or unexpected server errors.

## Technologies Used

*   **Java 17+**
*   **Spring Boot**: Framework for building robust, stand-alone, production-grade applications.
*   **Spring Data JPA**: For easy interaction with relational databases.
*   **Database**: (e.g., H2 for development, PostgreSQL/MySQL for production - *specify your actual database*)
*   **Message Broker (Choose one based on your actual setup):**
    *   **Spring for Apache Kafka**: For event streaming.
    *   **Spring Cloud GCP Pub/Sub**: For cloud-native messaging.
*   **SLF4J & Logback**: For flexible and high-performance logging.
*   **Jackson**: For JSON serialization/deserialization (used in DTOs and potentially message broker serialization).
*   **Maven** (or Gradle - *specify your actual build tool*)

## Setup and Installation

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Maven (or Gradle)
*   (If using Kafka) A running Kafka broker instance.
*   (If using Google Cloud Pub/Sub) A Google Cloud Platform project with Pub/Sub API enabled and authentication configured (e.g., via `gcloud auth application-default login` or service account key).
*   Your chosen database (e.g., PostgreSQL, MySQL) or H2 for local testing.

### Steps

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/contact_me_app.git
    cd contact_me_app
    ```

2.  **Configure `application.properties` (or `application.yml`):**
    Create `src/main/resources/application.properties` (if it doesn't exist) and configure your database, message broker, and application-specific properties.

    Example for **Database**:
    ```properties
    spring.datasource.url=jdbc:h2:mem:contactdb
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=
    spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
    spring.jpa.hibernate.ddl-auto=update # or validate, create, create-drop
    ```

    Example for **Kafka**:
    ```properties
    spring.kafka.bootstrap-servers=localhost:9092
    spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
    spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
    spring.kafka.producer.properties.spring.json.add-type-headers=false
    ```

    Example for **Google Cloud Pub/Sub**:
    ```properties
    spring.cloud.gcp.project-id=your-gcp-project-id
    # Other GCP properties if needed (e.g., credentials file path)
    ```

    Example for **Rate Limiting & Cleanup**:
    ```properties
    rate-limit.max-overall-requests=3
    rate-limit.overall-window-minutes=1440 # 24 hours
    rate-limit.cooldown-minutes=5
    data-cleanup.days-old=20
    ```

3.  **Build the project:**
    ```bash
    # Using Maven
    mvn clean install
    ```
    (Or `gradle build` if using Gradle)

## Running the Application

After building, you can run the Spring Boot application:

```bash
# Using Maven
mvn spring-boot:run
```
(Or `java -jar target/contact-me-app-0.0.1-SNAPSHOT.jar` if you built the JAR, replacing the version number with your actual one.)

The application will typically start on `http://localhost:8080`.

## API Endpoints

### 1. Submit Contact Form

Submits a new contact message. The request is subject to rate limiting.

*   **URL:** `/api/contact`
*   **Method:** `POST`
*   **Content-Type:** `application/json`
*   **Request Body Example:**
    ```json
    {
        "fullName": "John Doe",
        "email": "john.doe@example.com",
        "subject": "Inquiry about your service",
        "messageText": "Hello, I'd like to know more about your contact app."
    }
    ```
*   **Responses:**
    *   `200 OK`: "Contact form submitted successfully!"
    *   `400 Bad Request`: If validation fails or JSON is malformed (e.g., `MethodArgumentNotValidException`, `HttpMessageNotReadableException`).
    *   `429 Too Many Requests`: If rate-limited (`RateLimitInterceptor`), with `Retry-After` header.
    *   `500 Internal Server Error`: For unexpected server-side issues (`UnexpectedException`, `NullPointerException`).

## Logging

This project uses **SLF4J as a logging facade** with **Logback as the concrete logging implementation**.

### Configuration

Logging behavior is configured via `src/main/resources/logback.xml`.

A basic `logback.xml` might look like this (ensure this file exists in your project):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Console Appender: Logs to standard output (console) -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- File Appender: Logs to a file, with daily rolling -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file> <!-- Log file path -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- daily rollover -->
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <!-- keep 30 days' worth of history -->
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Root Logger: Sets the default logging level for the entire application -->
    <!-- Levels: TRACE, DEBUG, INFO, WARN, ERROR -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>

    <!-- Example: Set specific package to DEBUG level for more detailed logs -->
    <!--
    <logger name="com.contactme.contact_me_app" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </logger>
    -->

</configuration>
```

### Usage in Code

Loggers are initialized per class:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClass {
    private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

    public void myMethod() {
        logger.trace("A very fine-grained log.");
        logger.debug("Debugging information: {}", someVariable);
        logger.info("Informational message.");
        logger.warn("A potentially harmful situation occurred.");
        logger.error("An error occurred: {}", e.getMessage(), e); // Log exception with stack trace
    }
}
```

## Exception Handling

The application features centralized exception handling using `@ControllerAdvice`.

*   **`NullPointerException`**: Catches `NullPointerException` and returns an HTTP 500 error.
*   **`MethodArgumentNotValidException`**: Handles Spring's validation errors (e.g., from `@Valid` annotation) returning HTTP 400 with detailed field errors.
*   **`HttpMessageNotReadableException`**: Catches malformed request bodies (e.g., invalid JSON) returning HTTP 400.
*   **`UnexpectedException`**: A custom application-specific exception for internal server errors, returning HTTP 500.

This setup ensures consistent and informative error responses from the API.
