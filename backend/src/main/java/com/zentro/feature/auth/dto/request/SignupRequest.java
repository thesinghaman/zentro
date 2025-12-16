package com.zentro.feature.auth.dto.request;

import com.zentro.common.util.Constants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user signup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    
    @NotBlank(message = Constants.VALIDATION_FIRSTNAME_REQUIRED)
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = Constants.VALIDATION_LASTNAME_REQUIRED)
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = Constants.VALIDATION_EMAIL_REQUIRED)
    @Email(message = Constants.VALIDATION_EMAIL_INVALID)
    private String email;
    
    @NotBlank(message = Constants.VALIDATION_PASSWORD_REQUIRED)
    @Size(min = 8, message = Constants.VALIDATION_PASSWORD_MIN_LENGTH)
    private String password;
}
