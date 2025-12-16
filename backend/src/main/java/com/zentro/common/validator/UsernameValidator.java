package com.zentro.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for usernames
 * Checks for offensive, reserved, and system usernames
 */
public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    // Reserved system usernames
    private static final List<String> RESERVED_USERNAMES = Arrays.asList(
            "admin", "administrator", "root", "system", "moderator", "mod",
            "support", "help", "api", "null", "undefined", "test", "demo",
            "guest", "user", "default", "www", "ftp", "mail", "smtp", "pop",
            "imap", "http", "https", "ssh", "blog", "forum", "shop", "store",
            "app", "application", "service", "server", "database", "db",
            "zentro", "payment", "checkout", "cart", "order", "invoice"
    );

    // Offensive/inappropriate words (basic list - expand as needed)
    private static final List<String> OFFENSIVE_WORDS = Arrays.asList(
            "fuck", "shit", "ass", "bitch", "damn", "hell", "crap",
            "nazi", "hitler", "terrorist", "rape", "drug", "cocaine",
            "porn", "xxx", "sex", "pussy", "dick", "cock", "penis"
    );

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        String lowerUsername = username.toLowerCase();

        // Check for reserved usernames
        if (RESERVED_USERNAMES.contains(lowerUsername)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("This username is reserved and cannot be used")
                    .addConstraintViolation();
            return false;
        }

        // Check for offensive words
        for (String offensiveWord : OFFENSIVE_WORDS) {
            if (lowerUsername.contains(offensiveWord)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Username contains inappropriate content")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
