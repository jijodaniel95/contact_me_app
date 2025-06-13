package com.contactme.contact_me_app.exception;

public class UnexpectedException extends RuntimeException {
    public UnexpectedException(String message) {
        super(message);
    }
    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
