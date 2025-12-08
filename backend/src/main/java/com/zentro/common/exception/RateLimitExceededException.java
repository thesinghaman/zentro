package com.zentro.common.exception;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends ZentroException {
    
    public RateLimitExceededException(String message) {
        super(message);
    }
}
