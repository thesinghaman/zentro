package com.zentro.feature.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for password reset OTP verification
 * Returns temporary token for password reset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryTokenResponse {
    
    private String temporaryToken;
    private Long expiresIn;
    private String message;
}
