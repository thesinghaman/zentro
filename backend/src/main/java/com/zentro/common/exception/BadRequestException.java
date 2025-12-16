package com.zentro.common.exception;

/**
 * Exception thrown when a client request is invalid or malformed
 */
public class BadRequestException extends ZentroException{
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
