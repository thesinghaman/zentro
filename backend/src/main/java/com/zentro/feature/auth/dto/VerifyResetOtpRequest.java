package com.zentro.feature.auth.dto;

import com.zentro.common.util.Constants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying password reset OTP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResetOtpRequest {
    
    @NotBlank(message = Constants.VALIDATION_EMAIL_REQUIRED)
    @Email(message = Constants.VALIDATION_EMAIL_INVALID)
    private String email;
    
    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;
}
