package com.zentro.feature.auth.service.impl;

import com.zentro.common.exception.RateLimitExceededException;
import com.zentro.common.exception.ValidationException;
import com.zentro.common.util.Constants;
import com.zentro.feature.auth.entity.OtpVerification;
import com.zentro.feature.auth.repository.OtpVerificationRepository;
import com.zentro.feature.auth.service.OtpService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OTP Service implementation for generating, validating, and managing OTPs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.otp.length:6}")
    private int otpLength;

    @Value("${app.security.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${app.security.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.otp.rate-limit.max-requests:3}")
    private int rateLimitMaxRequests;

    @Value("${app.security.otp.rate-limit.window-hours:1}")
    private int rateLimitWindowHours;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generate and store OTP for user
     */
    @Transactional
    public String generateOtp(Long userId, String email, String purpose) {
        // Check rate limiting (existing hour-based rate limit)
        checkRateLimit(email, purpose);

        // Check cooldown and resend attempts
        checkCooldownAndResendLimit(email, purpose);

        // Delete existing OTPs for this user and purpose
        if (userId != null) {
            otpRepository.deleteByUserIdAndPurpose(userId, purpose);
        } else {
            otpRepository.deleteByEmailAndPurpose(email, purpose);
        }

        // Generate OTP
        String otp = generateRandomOtp();
        String otpHash = passwordEncoder.encode(otp);

        // Save OTP with rate limiting fields
        OtpVerification otpVerification = OtpVerification.builder()
                .userId(userId)
                .email(email)
                .otpHash(otpHash)
                .purpose(purpose)
                .attempts(0)
                .maxAttempts(maxAttempts)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .lastOtpSentAt(LocalDateTime.now())
                .otpResendCount(0)
                .build();

        otpRepository.save(otpVerification);

        log.info("OTP generated for email: {} with purpose: {}", email, purpose);
        return otp;
    }

    /**
     * Validate OTP
     */
    @Transactional
    public boolean validateOtp(Long userId, String email, String otp, String purpose) {
        Optional<OtpVerification> otpVerificationOpt;

        if (userId != null) {
            otpVerificationOpt = otpRepository.findFirstByUserIdAndPurposeOrderByCreatedAtDesc(userId, purpose);
        } else {
            otpVerificationOpt = otpRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);
        }

        if (otpVerificationOpt.isEmpty()) {
            throw new ValidationException(Constants.ERROR_INVALID_OTP);
        }

        OtpVerification otpVerification = otpVerificationOpt.get();

        // Check if expired
        if (otpVerification.isExpired()) {
            throw new ValidationException(Constants.ERROR_OTP_EXPIRED);
        }

        // Check if max attempts exceeded
        if (otpVerification.isMaxAttemptsExceeded()) {
            throw new ValidationException(Constants.ERROR_MAX_OTP_ATTEMPTS);
        }

        // Increment attempts
        otpVerification.incrementAttempts();
        otpRepository.save(otpVerification);

        // Validate OTP
        boolean isValid = passwordEncoder.matches(otp, otpVerification.getOtpHash());

        if (isValid) {
            // Delete OTP after successful validation
            otpRepository.delete(otpVerification);
            log.info("OTP validated successfully for email: {}", email);
        } else {
            log.warn("Invalid OTP attempt for email: {}. Attempts: {}/{}",
                    email, otpVerification.getAttempts(), maxAttempts);
        }

        return isValid;
    }

    /**
     * Check rate limiting for OTP generation
     */
    private void checkRateLimit(String email, String purpose) {
        LocalDateTime windowStart = LocalDateTime.now().minusHours(rateLimitWindowHours);
        long requestCount = otpRepository.countByEmailAndPurposeAndCreatedAtAfter(email, purpose, windowStart);

        if (requestCount >= rateLimitMaxRequests) {
            log.warn("Rate limit exceeded for email: {} with purpose: {}", email, purpose);
            throw new RateLimitExceededException(Constants.ERROR_OTP_RATE_LIMIT);
        }
    }

    /**
     * Check cooldown period (60 seconds) and resend attempts limit (5 per hour)
     */
    private void checkCooldownAndResendLimit(String email, String purpose) {
        // Get the most recent OTP record
        Optional<OtpVerification> recentOtpOpt = otpRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);

        if (recentOtpOpt.isPresent()) {
            OtpVerification recentOtp = recentOtpOpt.get();
            LocalDateTime now = LocalDateTime.now();

            // Check if 60-second cooldown has passed
            if (recentOtp.getLastOtpSentAt() != null) {
                long secondsSinceLastSent = java.time.Duration.between(recentOtp.getLastOtpSentAt(), now).getSeconds();

                if (secondsSinceLastSent < 60) {
                    long remainingSeconds = 60 - secondsSinceLastSent;
                    log.warn("OTP cooldown active for email: {}. Remaining: {} seconds", email, remainingSeconds);
                    throw new RateLimitExceededException(
                            String.format("Please wait %d seconds before requesting a new OTP", remainingSeconds)
                    );
                }
            }

            // Check resend attempts limit (5 per hour)
            if (recentOtp.getLastOtpSentAt() != null) {
                long hoursSinceFirstSent = java.time.Duration.between(recentOtp.getLastOtpSentAt(), now).toHours();

                // Reset counter if more than 1 hour has passed
                if (hoursSinceFirstSent >= 1) {
                    recentOtp.setOtpResendCount(0);
                    otpRepository.save(recentOtp);
                } else if (recentOtp.getOtpResendCount() >= 5) {
                    // Max attempts exceeded within the hour
                    log.warn("OTP resend limit exceeded for email: {}. Attempts: {}", email, recentOtp.getOtpResendCount());
                    throw new RateLimitExceededException(
                            "Maximum OTP resend attempts exceeded. Please try again in 1 hour"
                    );
                }
            }

            // Increment resend counter
            recentOtp.setOtpResendCount(recentOtp.getOtpResendCount() + 1);
            recentOtp.setLastOtpSentAt(now);
            otpRepository.save(recentOtp);
        }
    }

    /**
     * Generate random OTP
     */
    private String generateRandomOtp() {
        int bound = (int) Math.pow(10, otpLength);
        int otp = random.nextInt(bound);
        return String.format("%0" + otpLength + "d", otp);
    }

    /**
     * Cleanup expired OTPs (runs every hour)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredOtps() {
        try {
            otpRepository.deleteExpiredOtps(LocalDateTime.now());
            log.info("Expired OTPs cleaned up successfully");
        } catch (Exception e) {
            log.error("Error cleaning up expired OTPs", e);
        }
    }
}
