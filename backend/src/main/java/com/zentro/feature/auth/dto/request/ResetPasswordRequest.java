package com.zentro.feature.auth.dto.request;

import com.zentro.common.util.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resetting password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    
    @NotBlank(message = "Temporary token is required")
    private String temporaryToken;
    
    @NotBlank(message = Constants.VALIDATION_PASSWORD_REQUIRED)
    @Size(min = 8, message = Constants.VALIDATION_PASSWORD_MIN_LENGTH)
    private String newPassword;
}
