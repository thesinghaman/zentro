package com.zentro.common.util;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Utility class for generating secure public-facing IDs
 * Format: PREFIX-TIMESTAMP-RANDOM (e.g., USR-1733707200-A7X9F2)
 */
public class PublicIdGenerator {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RANDOM_LENGTH = 6;
    
    /**
     * Generate a public ID for a user
     * Format: USR-{timestamp}-{random}
     */
    public static String generateUserId() {
        return generate("USR");
    }
    
    /**
     * Generate a public ID with custom prefix
     * @param prefix The prefix for the ID (e.g., "USR", "ORD", "PRD")
     * @return Generated public ID
     */
    public static String generate(String prefix) {
        long timestamp = Instant.now().getEpochSecond();
        String randomPart = generateRandomString(RANDOM_LENGTH);
        return String.format("%s-%d-%s", prefix, timestamp, randomPart);
    }
    
    /**
     * Generate a random alphanumeric string
     */
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
