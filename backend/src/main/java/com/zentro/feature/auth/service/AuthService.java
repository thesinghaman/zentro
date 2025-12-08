package com.zentro.feature.auth.service;

import com.zentro.feature.auth.dto.*;

/**
 * Service interface for authentication operations
 */
public interface AuthService {
    
    /**
     * Register a new user and send verification OTP
     * 
     * @param request Signup request containing user details
     * @return SignupResponse with user ID and message
     */
    SignupResponse signup(SignupRequest request);
    
    /**
     * Login user with email and password
     * User must be email verified to login
     * 
     * @param request Login request containing credentials
     * @return JwtResponse with access/refresh tokens and user data
     */
    JwtResponse login(LoginRequest request);
    
    /**
     * Verify email with OTP and complete registration
     * Returns JWT tokens for automatic login
     * 
     * @param request OTP verification request
     * @return JwtResponse with access/refresh tokens and user data
     */
    JwtResponse verifyEmail(VerifyOtpRequest request);
    
    /**
     * Resend email verification OTP
     * 
     * @param request Resend OTP request with user ID
     * @return Message confirming OTP was sent
     */
    String resendVerificationOtp(ResendOtpRequest request);
    
    /**
     * Initiate forgot password flow by sending OTP
     * 
     * @param request Forgot password request with email
     * @return Message confirming OTP was sent
     */
    String forgotPassword(ForgotPasswordRequest request);
    
    /**
     * Verify password reset OTP and return temporary token
     * 
     * @param request OTP verification request
     * @return TemporaryTokenResponse with short-lived token for password reset
     */
    TemporaryTokenResponse verifyResetOtp(VerifyResetOtpRequest request);
    
    /**
     * Reset password using temporary token from OTP verification
     * 
     * @param request Reset password request with temporary token and new password
     * @return Confirmation message
     */
    String resetPassword(ResetPasswordRequest request);
    
    /**
     * Refresh access token using refresh token
     * 
     * @param refreshToken Refresh token string
     * @return JwtResponse with new access token and same refresh token
     */
    JwtResponse refreshAccessToken(String refreshToken);
    
    /**
     * Logout user by invalidating refresh token
     * 
     * @param userId User ID
     */
    void logout(Long userId);
}
