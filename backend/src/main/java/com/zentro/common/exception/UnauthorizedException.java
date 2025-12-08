package com.zentro.common.exception;

/**
 * Exception thrown when authentication or authorization fails
 */
public class UnauthorizedException extends ZentroException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
}
