package com.zentro.common.exception;

/**
 * Exception thrown when business validation fails
 */
public class ValidationException extends ZentroException {
    
    public ValidationException(String message) {
        super(message);
    }
}
