package com.zentro.common.exception;

/**
 * Exception thrown when a duplicate resource is attempted to be created
 */
public class DuplicateResourceException extends ZentroException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
}
