package com.zentro.common.exception;

/**
 * Exception thrown when account is locked
 */
public class AccountLockedException extends ZentroException {
    
    public AccountLockedException(String message) {
        super(message);
    }
}
