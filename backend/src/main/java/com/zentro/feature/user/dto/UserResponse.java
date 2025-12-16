package com.zentro.feature.user.dto;

import com.zentro.feature.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for User data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private String id; // Public ID (e.g., USR-1765228263-GPHI6G)
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private Boolean emailVerified;
    private String role;
    
    /**
     * Convert User entity to UserResponse DTO
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getPublicId()) // Return public ID instead of internal ID
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.getEmailVerified())
                .role(user.getRole().name())
                .build();
    }
}
