package com.zentro.feature.auth.dto.request;

import com.zentro.common.util.Constants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = Constants.VALIDATION_EMAIL_REQUIRED)
    @Email(message = Constants.VALIDATION_EMAIL_INVALID)
    private String email;
    
    @NotBlank(message = Constants.VALIDATION_PASSWORD_REQUIRED)
    private String password;
}
