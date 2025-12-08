package com.zentro.feature.auth.dto;

import com.zentro.common.util.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for forgot password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    
    @NotBlank(message = Constants.VALIDATION_EMAIL_REQUIRED)
    @Email(message = Constants.VALIDATION_EMAIL_INVALID)
    private String email;
}
