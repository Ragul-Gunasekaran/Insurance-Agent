package com.ubi.platform.exception;

/**
 * Exception thrown when attempting to create a resource that violates business uniqueness constraints.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
