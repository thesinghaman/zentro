package com.zentro.feature.user.controller;

import com.zentro.common.dto.ApiResponse;
import com.zentro.common.security.UserPrincipal;
import com.zentro.common.util.Constants;
import com.zentro.feature.user.dto.request.UpdateProfileRequest;
import com.zentro.feature.user.dto.request.UpdateUsernameRequest;
import com.zentro.feature.user.dto.response.UserResponse;
import com.zentro.feature.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * REST Controller for user profile endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    /**
     * GET /api/v1/users/profile
     * Get current user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Getting profile for user: {}", userPrincipal.getId());
        UserResponse user = userService.getProfile(userPrincipal.getId());

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Profile retrieved successfully")
                        .data(user)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * PUT /api/v1/users/profile
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("Updating profile for user: {}", userPrincipal.getId());
        UserResponse user = userService.updateProfile(userPrincipal.getId(), request);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Profile updated successfully")
                        .data(user)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * PUT /api/v1/users/username
     * Update username
     */
    @PutMapping("/username")
    public ResponseEntity<ApiResponse<UserResponse>> updateUsername(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateUsernameRequest request) {

        log.info("Updating username for user: {}", userPrincipal.getId());
        UserResponse user = userService.updateUsername(userPrincipal.getId(), request);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Username updated successfully")
                        .data(user)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * PUT /api/v1/users/profile/picture
     * Upload/update profile picture
     */
    @PutMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePicture(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file) {

        log.info("Updating profile picture for user: {}", userPrincipal.getId());
        UserResponse user = userService.updateProfilePicture(userPrincipal.getId(), file);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Profile picture updated successfully")
                        .data(user)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * DELETE /api/v1/users/profile/picture
     * Delete profile picture
     */
    @DeleteMapping("/profile/picture")
    public ResponseEntity<ApiResponse<UserResponse>> deleteProfilePicture(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Deleting profile picture for user: {}", userPrincipal.getId());
        UserResponse user = userService.deleteProfilePicture(userPrincipal.getId());

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Profile picture deleted successfully")
                        .data(user)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * DELETE /api/v1/users/profile
     * Soft delete user account (can be restored within 30 days by logging in)
     */
    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Deleting account for user: {}", userPrincipal.getId());
        userService.deleteAccount(userPrincipal.getId());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(Constants.SUCCESS_ACCOUNT_DELETED)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}