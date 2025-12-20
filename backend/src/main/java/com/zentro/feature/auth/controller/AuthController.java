package com.zentro.feature.auth.controller;

import com.zentro.common.util.Constants;
import com.zentro.common.dto.ApiResponse;
import com.zentro.common.exception.UnauthorizedException;
import com.zentro.common.security.JwtTokenProvider;
import com.zentro.feature.auth.dto.request.*;
import com.zentro.feature.auth.dto.response.JwtResponse;
import com.zentro.feature.auth.dto.response.SignupResponse;
import com.zentro.feature.auth.dto.response.TemporaryTokenResponse;
import com.zentro.feature.auth.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.admin.secret-key}")
    private String adminSecretKey;
    
    /**
     * POST /api/v1/auth/signup
     * Register new user and send verification OTP
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        log.info("POST /api/v1/auth/signup - Email: {}", request.getEmail());
        SignupResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. Please verify your email.", response));
    }

    /**
     * POST /api/v1/auth/admin/signup
     * Register new admin user with secret key verification
     */
    @PostMapping("/admin/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> adminSignup(
            @RequestHeader(value = "Admin-Secret-Key", required = true) String providedSecret,
            @Valid @RequestBody SignupRequest request) {
        log.info("POST /api/v1/auth/admin/signup - Email: {}", request.getEmail());

        // Verify admin secret key
        if (!providedSecret.equals(adminSecretKey)) {
            log.warn("Invalid admin secret key attempt for email: {}", request.getEmail());
            throw new UnauthorizedException(Constants.ERROR_INVALID_ADMIN_SECRET);
        }

        // Create admin user
        SignupResponse response = authService.adminSignup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(Constants.SUCCESS_ADMIN_CREATED, response));
    }
    
    /**
     * POST /api/v1/auth/login
     * Login with email and password
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - Email: {}", request.getEmail());
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
    
    /**
     * POST /api/v1/auth/verify-email
     * Verify email with OTP
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<JwtResponse>> verifyEmail(
            @Valid @RequestBody VerifyOtpRequest request) {
        log.info("POST /api/v1/auth/verify-email - Email: {}", request.getEmail());
        JwtResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", response));
    }
    
    /**
     * POST /api/v1/auth/resend-otp
     * Resend verification OTP
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {
        log.info("POST /api/v1/auth/resend-otp - Email: {}", request.getEmail());
        String message = authService.resendVerificationOtp(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }
    
    /**
     * POST /api/v1/auth/forgot-password
     * Initiate forgot password flow
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/v1/auth/forgot-password - Email: {}", request.getEmail());
        String message = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }
    
    /**
     * POST /api/v1/auth/verify-reset-otp
     * Verify password reset OTP
     */
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<TemporaryTokenResponse>> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request) {
        log.info("POST /api/v1/auth/verify-reset-otp - Email: {}", request.getEmail());
        TemporaryTokenResponse response = authService.verifyResetOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
    }
    
    /**
     * POST /api/v1/auth/reset-password
     * Reset password with temporary token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/v1/auth/reset-password");
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }
    
    /**
     * POST /api/v1/auth/refresh
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        log.info("POST /api/v1/auth/refresh");
        
        // Extract refresh token from Authorization header
        String refreshToken = authHeader.replace("Bearer ", "");
        JwtResponse response = authService.refreshAccessToken(refreshToken);
        
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }
    
    /**
     * POST /api/v1/auth/logout
     * Logout user (invalidate refresh token)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        // Extract token and get userId from JWT
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        log.info("POST /api/v1/auth/logout - User ID: {}", userId);
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
