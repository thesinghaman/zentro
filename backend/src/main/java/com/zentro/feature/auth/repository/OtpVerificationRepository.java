package com.zentro.feature.auth.repository;

import com.zentro.feature.auth.entity.OtpVerification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for OtpVerification entity
 */
@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    
    /**
     * Find latest OTP by user ID and purpose
     */
    Optional<OtpVerification> findFirstByUserIdAndPurposeOrderByCreatedAtDesc(Long userId, String purpose);
    
    /**
     * Find latest OTP by email and purpose (for password reset without userId)
     */
    Optional<OtpVerification> findFirstByEmailAndPurposeOrderByCreatedAtDesc(String email, String purpose);
    
    /**
     * Count OTP requests by email and purpose within time window (for rate limiting)
     */
    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.email = :email AND o.purpose = :purpose AND o.createdAt > :since")
    long countByEmailAndPurposeAndCreatedAtAfter(String email, String purpose, LocalDateTime since);
    
    /**
     * Delete expired OTPs (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(LocalDateTime now);
    
    /**
     * Delete OTPs by user ID and purpose
     */
    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.userId = :userId AND o.purpose = :purpose")
    void deleteByUserIdAndPurpose(Long userId, String purpose);
    
    /**
     * Delete OTPs by email and purpose
     */
    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.email = :email AND o.purpose = :purpose")
    void deleteByEmailAndPurpose(String email, String purpose);
}
