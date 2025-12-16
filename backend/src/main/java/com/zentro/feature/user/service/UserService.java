package com.zentro.feature.user.service;

import com.zentro.feature.user.dto.response.UserResponse;
import com.zentro.feature.user.dto.request.UpdateProfileRequest;
import com.zentro.feature.user.dto.request.UpdateUsernameRequest;

import org.springframework.web.multipart.MultipartFile;

/**
 * User Service interface for user profile operations
 */
public interface UserService {

    /**
     * Get user profile by user ID
     */
    UserResponse getProfile(Long userId);

    /**
     * Update user profile
     */
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * Update username (must be unique)
     */
    UserResponse updateUsername(Long userId, UpdateUsernameRequest request);

    /**
     * Upload/update profile picture
     */
    UserResponse updateProfilePicture(Long userId, MultipartFile file);

    /**
     * Delete profile picture
     */
    UserResponse deleteProfilePicture(Long userId);

    /**
     * Soft delete user account (sets isDeleted=true, locks account)
     * Account can be restored within 30 days by logging in
     */
    void deleteAccount(Long userId);
}
