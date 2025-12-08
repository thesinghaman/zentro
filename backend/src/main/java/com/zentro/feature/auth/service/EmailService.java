package com.zentro.feature.auth.service;

/**
 * Email Service interface for sending emails
 */
public interface EmailService {
    
    /**
     * Send OTP email for email verification
     */
    void sendVerificationOtp(String toEmail, String firstName, String otp);
    
    /**
     * Send OTP email for password reset
     */
    void sendPasswordResetOtp(String toEmail, String firstName, String otp);
    
    /**
     * Send welcome email after successful registration
     */
    void sendWelcomeEmail(String toEmail, String firstName);
    
    /**
     * Send password reset confirmation email
     */
    void sendPasswordResetConfirmation(String toEmail, String firstName);
}
