package com.zentro.feature.user.service.impl;

import com.zentro.common.exception.BadRequestException;
import com.zentro.common.exception.ResourceNotFoundException;
import com.zentro.common.service.R2StorageService;
import com.zentro.common.util.Constants;
import com.zentro.feature.user.dto.request.UpdateProfileRequest;
import com.zentro.feature.user.dto.request.UpdateUsernameRequest;
import com.zentro.feature.user.dto.response.UserResponse;
import com.zentro.feature.user.entity.User;
import com.zentro.feature.user.repository.UserRepository;
import com.zentro.feature.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * User Service implementation for user profile operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final R2StorageService r2StorageService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = findUserById(userId);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);

        return UserResponse.from(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUsername(Long userId, UpdateUsernameRequest request) {
        User user = findUserById(userId);

        // Check 30-day cooldown
        if (user.getLastUsernameChangedAt() != null) {
            long daysSinceLastChange = java.time.temporal.ChronoUnit.DAYS.between(
                    user.getLastUsernameChangedAt(),
                    java.time.LocalDateTime.now()
            );

            if (daysSinceLastChange < Constants.USERNAME_CHANGE_COOLDOWN_DAYS) {
                long daysRemaining = Constants.USERNAME_CHANGE_COOLDOWN_DAYS - daysSinceLastChange;
                throw new BadRequestException(
                        String.format(Constants.ERROR_USERNAME_COOLDOWN, daysRemaining)
                );
            }
        }

        // Check if username is already taken by another user
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
            throw new BadRequestException(Constants.ERROR_USERNAME_ALREADY_TAKEN);
        }

        user.setUsername(request.getUsername());
        user.setLastUsernameChangedAt(java.time.LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        log.info("Username updated for user: {} to: {}", userId, request.getUsername());

        return UserResponse.from(updatedUser);
    }

    @Override
    public UserResponse updateProfilePicture(Long userId, MultipartFile file) {
        User user = findUserById(userId);

        // Delete old profile picture if exists
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isBlank()) {
            r2StorageService.deleteFile(user.getProfilePictureUrl());
        }

        // Upload new profile picture to R2
        String profilePictureUrl = r2StorageService.uploadFile(file, Constants.R2_FOLDER_PROFILE_PICTURES);
        user.setProfilePictureUrl(profilePictureUrl);

        User updatedUser = userRepository.save(user);
        log.info("Profile picture updated for user: {}", userId);

        return UserResponse.from(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse deleteProfilePicture(Long userId) {
        User user = findUserById(userId);

        if (user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isBlank()) {
            throw new BadRequestException("No profile picture to delete");
        }

        // Delete file from R2
        r2StorageService.deleteFile(user.getProfilePictureUrl());

        // Remove URL from user
        user.setProfilePictureUrl(null);
        User updatedUser = userRepository.save(user);
        log.info("Profile picture deleted for user: {}", userId);

        return UserResponse.from(updatedUser);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        User user = findUserById(userId);

        if (user.getIsDeleted()) {
            throw new BadRequestException(Constants.ERROR_ACCOUNT_ALREADY_DELETED);
        }

        // Soft delete - mark as deleted but keep data
        user.setIsDeleted(true);
        user.setDeletedAt(java.time.LocalDateTime.now());

        // Lock account to prevent login
        user.setAccountLockedUntil(java.time.LocalDateTime.now().plusYears(Constants.ACCOUNT_LOCK_YEARS));

        userRepository.save(user);
        log.info("Account deleted for user ID: {}", userId);
    }

    /**
     * Find user by ID or throw exception
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
