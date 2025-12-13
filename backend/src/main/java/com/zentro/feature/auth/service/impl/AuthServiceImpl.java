package com.zentro.feature.auth.service.impl;

import com.zentro.common.exception.DuplicateResourceException;
import com.zentro.common.exception.ResourceNotFoundException;
import com.zentro.common.exception.UnauthorizedException;
import com.zentro.common.security.JwtTokenProvider;
import com.zentro.common.util.Constants;
import com.zentro.common.util.PublicIdGenerator;
import com.zentro.feature.auth.dto.*;
import com.zentro.feature.auth.entity.RefreshToken;
import com.zentro.feature.auth.entity.Role;
import com.zentro.feature.auth.entity.User;
import com.zentro.feature.auth.repository.RefreshTokenRepository;
import com.zentro.feature.auth.repository.UserRepository;
import com.zentro.feature.auth.service.AuthService;
import com.zentro.feature.auth.service.EmailService;
import com.zentro.feature.auth.service.OtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of authentication service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${app.jwt.access-token-expiration}")
    private Long accessTokenExpiration;
    
    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    
    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("Signup request received for email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new DuplicateResourceException(Constants.ERROR_EMAIL_EXISTS);
        }
        
        // Create new user
        User user = User.builder()
                .publicId(PublicIdGenerator.generateUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(generateUsername(request.getEmail()))
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .role(Role.USER)
                .failedOtpAttempts(0)
                .isDeleted(false)
                .build();
        
        user = userRepository.save(user);
        log.info("User created with ID: {} (publicId: {})", user.getId(), user.getPublicId());
        
        // Generate and send OTP
        String otp = otpService.generateOtp(user.getId(), user.getEmail(), "EMAIL_VERIFICATION");
        emailService.sendVerificationOtp(user.getEmail(), user.getFirstName(), otp);
        
        return SignupResponse.builder()
                .userId(user.getPublicId()) // Return public ID
                .email(user.getEmail())
                .message(Constants.SUCCESS_SIGNUP)
                .build();
    }
    
    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        
        // Find user
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(Constants.ERROR_INVALID_CREDENTIALS));
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new UnauthorizedException(Constants.ERROR_ACCOUNT_LOCKED);
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException(Constants.ERROR_INVALID_CREDENTIALS);
        }
        
        // Check if email is verified
        if (!user.getEmailVerified()) {
            throw new UnauthorizedException(Constants.ERROR_EMAIL_NOT_VERIFIED);
        }
        
        // Generate tokens
        return generateJwtResponse(user);
    }
    
    @Override
    @Transactional
    public JwtResponse verifyEmail(VerifyOtpRequest request) {
        log.info("Email verification request received for email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new UnauthorizedException(Constants.ERROR_ACCOUNT_LOCKED);
        }
        
        // Validate OTP
        boolean isValid = otpService.validateOtp(
                user.getId(),
                user.getEmail(),
                request.getOtp(),
                "EMAIL_VERIFICATION"
        );
        
        if (!isValid) {
            user.incrementFailedOtpAttempts();
            if (user.getFailedOtpAttempts() >= 10) {
                user.lockAccount(60); // Lock for 1 hour
                userRepository.save(user);
                throw new UnauthorizedException(Constants.ERROR_ACCOUNT_LOCKED);
            }
            userRepository.save(user);
            throw new UnauthorizedException(Constants.ERROR_INVALID_OTP);
        }
        
        // Mark email as verified and reset failed attempts
        user.setEmailVerified(true);
        user.resetFailedOtpAttempts();
        user = userRepository.save(user);
        
        log.info("Email verified successfully for user ID: {}", user.getId());
        
        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        
        // Generate tokens for auto-login
        return generateJwtResponse(user);
    }
    
    @Override
    @Transactional
    public String resendVerificationOtp(ResendOtpRequest request) {
        log.info("Resend OTP request received for email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));
        
        // Check if already verified
        if (user.getEmailVerified()) {
            throw new UnauthorizedException("Email is already verified");
        }
        
        // Generate and send new OTP
        String otp = otpService.generateOtp(user.getId(), user.getEmail(), "EMAIL_VERIFICATION");
        emailService.sendVerificationOtp(user.getEmail(), user.getFirstName(), otp);
        
        return Constants.SUCCESS_OTP_SENT;
    }
    
    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request received for email: {}", request.getEmail());
        
        // Find user
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new UnauthorizedException(Constants.ERROR_ACCOUNT_LOCKED);
        }
        
        // Generate and send OTP
        String otp = otpService.generateOtp(user.getId(), user.getEmail(), "PASSWORD_RESET");
        emailService.sendPasswordResetOtp(user.getEmail(), user.getFirstName(), otp);
        
        return Constants.SUCCESS_OTP_SENT;
    }
    
    @Override
    @Transactional
    public TemporaryTokenResponse verifyResetOtp(VerifyResetOtpRequest request) {
        log.info("Verify reset OTP request received for email: {}", request.getEmail());
        
        // Find user
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new UnauthorizedException(Constants.ERROR_ACCOUNT_LOCKED);
        }
        
        // Validate OTP
        boolean isValid = otpService.validateOtp(
                user.getId(),
                user.getEmail(),
                request.getOtp(),
                "PASSWORD_RESET"
        );
        
        if (!isValid) {
            user.incrementFailedOtpAttempts();
            if (user.getFailedOtpAttempts() >= 10) {
                user.lockAccount(60); // Lock for 1 hour
                userRepository.save(user);
                throw new UnauthorizedException(Constants.ERROR_ACCOUNT_LOCKED);
            }
            userRepository.save(user);
            throw new UnauthorizedException(Constants.ERROR_INVALID_OTP);
        }
        
        // Reset failed attempts
        user.resetFailedOtpAttempts();
        userRepository.save(user);
        
        // Generate temporary token (5 minutes expiry)
        String temporaryToken = jwtTokenProvider.generateTemporaryToken(
                user.getId(),
                user.getEmail()
        );
        
        return TemporaryTokenResponse.builder()
                .temporaryToken(temporaryToken)
                .expiresIn(300L) // 5 minutes in seconds
                .message("OTP verified. Use this token to reset your password.")
                .build();
    }
    
    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request received");
        
        // Validate temporary token
        if (!jwtTokenProvider.validateToken(request.getTemporaryToken())) {
            throw new UnauthorizedException("Invalid or expired temporary token");
        }
        
        // Extract user ID from token
        Long userId = jwtTokenProvider.getUserIdFromToken(request.getTemporaryToken());
        
        // Find user
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedOtpAttempts();
        userRepository.save(user);
        
        // Invalidate all refresh tokens for this user
        refreshTokenRepository.deleteByUserId(userId);
        
        log.info("Password reset successfully for user ID: {}", userId);
        
        // Send confirmation email
        emailService.sendPasswordResetConfirmation(user.getEmail(), user.getFirstName());
        
        return Constants.SUCCESS_PASSWORD_RESET;
    }
    
    @Override
    @Transactional
    public JwtResponse refreshAccessToken(String refreshToken) {
        log.info("Refresh token request received");
        
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);
        }
        
        // Hash and find in database
        String tokenHash = jwtTokenProvider.hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException(Constants.ERROR_INVALID_TOKEN));
        
        // Check if expired
        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new UnauthorizedException(Constants.ERROR_TOKEN_EXPIRED);
        }
        
        // Find user
        User user = userRepository.findActiveById(storedToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));
        
        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
        
        return JwtResponse.of(
                newAccessToken,
                refreshToken,
                accessTokenExpiration,
                UserResponse.from(user)
        );
    }
    
    @Override
    @Transactional
    public void logout(Long userId) {
        log.info("Logout request received for user ID: {}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }
    
    /**
     * Generate JWT response with access and refresh tokens
     */
    private JwtResponse generateJwtResponse(User user) {
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
        
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId()
        );
        
        // Store refresh token in database
        String tokenHash = jwtTokenProvider.hashToken(refreshToken);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();
        
        refreshTokenRepository.save(refreshTokenEntity);
        
        return JwtResponse.of(
                accessToken,
                refreshToken,
                accessTokenExpiration,
                UserResponse.from(user)
        );
    }
    
    /**
     * Generate username from email (part before @)
     */
    private String generateUsername(String email) {
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int suffix = 1;
        
        // If username exists, append number
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }
        
        return username;
    }
}
