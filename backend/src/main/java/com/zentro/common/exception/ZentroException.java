package com.zentro.common.exception;

/**
 * Base exception for all custom exceptions in the application
 */
public class ZentroException extends RuntimeException {
    
    public ZentroException(String message) {
        super(message);
    }
    
    public ZentroException(String message, Throwable cause) {
        super(message, cause);
    }
}
