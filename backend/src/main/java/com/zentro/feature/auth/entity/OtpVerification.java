package com.zentro.feature.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * OTP Verification entity for email verification and password reset
 */
@Entity
@Table(name = "otp_verifications", indexes = {
        @Index(name = "idx_user_purpose", columnList = "user_id, purpose"),
        @Index(name = "idx_email_purpose", columnList = "email, purpose"),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_email_last_sent", columnList = "email, last_otp_sent_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;
    
    @Column(name = "purpose", nullable = false, length = 50)
    private String purpose; // EMAIL_VERIFICATION, PASSWORD_RESET
    
    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;
    
    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 5;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "last_otp_sent_at")
    private LocalDateTime lastOtpSentAt;
    
    @Column(name = "otp_resend_count", nullable = false)
    @Builder.Default
    private Integer otpResendCount = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if max attempts exceeded
     */
    public boolean isMaxAttemptsExceeded() {
        return attempts >= maxAttempts;
    }
    
    /**
     * Increment attempts
     */
    public void incrementAttempts() {
        this.attempts++;
    }
}
