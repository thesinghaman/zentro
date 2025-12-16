package com.zentro.feature.auth.service;

/**
 * OTP Service interface for generating, validating, and managing OTPs
 */
public interface OtpService {

    /**
     * Generate and store OTP for user
     *
     * @param userId  User ID
     * @param email   User email
     * @param purpose Purpose of OTP (e.g., "EMAIL_VERIFICATION", "PASSWORD_RESET")
     * @return Generated OTP code
     */
    String generateOtp(Long userId, String email, String purpose);

    /**
     * Validate OTP
     *
     * @param userId  User ID
     * @param email   User email
     * @param otp     OTP to validate
     * @param purpose Purpose of OTP
     * @return true if OTP is valid, false otherwise
     */
    boolean validateOtp(Long userId, String email, String otp, String purpose);

    /**
     * Cleanup expired OTPs (scheduled task)
     */
    @SuppressWarnings("unused")
    void cleanupExpiredOtps();
}
