package com.zentro.common.util;

/**
 * Application-wide constants
 */
public final class Constants {

    private Constants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // API
    public static final String API_VERSION = "/api/v1";

    // Roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // OTP
    public static final String OTP_PURPOSE_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    public static final String OTP_PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";

    // JWT
    public static final String JWT_CLAIM_USER_ID = "userId";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_ROLE = "role";
    public static final String JWT_TYPE_ACCESS = "ACCESS";
    public static final String JWT_TYPE_REFRESH = "REFRESH";
    public static final String JWT_TYPE_TEMPORARY = "TEMPORARY";

    // Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_BEARER_PREFIX = "Bearer ";

    // Date Format
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    // Validation Messages
    public static final String VALIDATION_EMAIL_REQUIRED = "Email is required";
    public static final String VALIDATION_EMAIL_INVALID = "Email is invalid";
    public static final String VALIDATION_PASSWORD_REQUIRED = "Password is required";
    public static final String VALIDATION_PASSWORD_MIN_LENGTH = "Password must be at least 8 characters";
    public static final String VALIDATION_FIRSTNAME_REQUIRED = "First name is required";
    public static final String VALIDATION_LASTNAME_REQUIRED = "Last name is required";

    // Error Messages
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ERROR_EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String ERROR_EMAIL_EXISTS = "Email already exists";
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_EMAIL_NOT_VERIFIED = "Email not verified";
    public static final String ERROR_INVALID_OTP = "Invalid or expired OTP";
    public static final String ERROR_OTP_EXPIRED = "OTP has expired";
    public static final String ERROR_MAX_OTP_ATTEMPTS = "Maximum OTP attempts exceeded";
    public static final String ERROR_OTP_RATE_LIMIT = "Too many OTP requests. Please try again later";
    public static final String ERROR_ACCOUNT_LOCKED = "Account is temporarily locked due to multiple failed attempts";
    public static final String ERROR_INVALID_TOKEN = "Invalid or expired token";
    public static final String ERROR_TOKEN_EXPIRED = "Token has expired";
    public static final String ERROR_UNAUTHORIZED = "Unauthorized access";

    // Success Messages
    public static final String SUCCESS_SIGNUP = "Account created successfully. Please verify your email";
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_EMAIL_VERIFIED = "Email verified successfully";
    public static final String SUCCESS_OTP_SENT = "OTP sent to your email";
    public static final String SUCCESS_PASSWORD_RESET = "Password reset successfully";
    public static final String SUCCESS_PROFILE_UPDATED = "Profile updated successfully";
    public static final String SUCCESS_USERNAME_UPDATED = "Username updated successfully";
    public static final String SUCCESS_PROFILE_PICTURE_UPDATED = "Profile picture updated successfully";
    public static final String SUCCESS_PROFILE_PICTURE_DELETED = "Profile picture deleted successfully";
    public static final String SUCCESS_ACCOUNT_DELETED = "Account deleted successfully. You can restore it within 30 days by logging in.";

    // Error Messages - Additional
    public static final String ERROR_EMAIL_ALREADY_VERIFIED = "Email is already verified";
    public static final String ERROR_INVALID_TEMPORARY_TOKEN = "Invalid or expired temporary token";
    public static final String ERROR_USERNAME_ALREADY_TAKEN = "Username already taken";
    public static final String ERROR_NO_PROFILE_PICTURE = "No profile picture to delete";
    public static final String ERROR_ACCOUNT_ALREADY_DELETED = "Account is already deleted";
    public static final String ERROR_ACCOUNT_PERMANENTLY_DELETED = "Account was permanently deleted. Please create a new account.";
    public static final String ERROR_ACCOUNT_PENDING_DELETION = "This account is scheduled for deletion. Please log in to restore it instead of creating a new account.";
    public static final String ERROR_USERNAME_COOLDOWN = "You can change your username again in %d days";

    // Time Periods
    public static final int ACCOUNT_DELETION_GRACE_PERIOD_DAYS = 30;
    public static final int USERNAME_CHANGE_COOLDOWN_DAYS = 30;
    public static final int OTP_MAX_FAILED_ATTEMPTS = 10;
    public static final int ACCOUNT_LOCK_DURATION_MINUTES = 60;
    public static final int OTP_RATE_LIMIT_MAX_RESENDS = 5;
    public static final int OTP_RATE_LIMIT_WINDOW_HOURS = 1;
    public static final int ACCOUNT_LOCK_YEARS = 100; // Used for soft-deleted accounts

    // Folders
    public static final String R2_FOLDER_PROFILE_PICTURES = "profile-pictures";
}
