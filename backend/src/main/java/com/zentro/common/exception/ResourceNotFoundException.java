package com.zentro.common.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends ZentroException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
