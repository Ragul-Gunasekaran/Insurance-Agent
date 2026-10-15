package com.ubi.platform.exception;

/**
 * Exception thrown when a requested domain entity or resource does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
