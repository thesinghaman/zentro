package com.zentro.feature.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for signup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    
    private String userId; // Public ID (e.g., USR-1733707200-A7X9F2)
    private String email;
    private String message;
}
