package com.zentro.common.security;

import com.zentro.common.util.Constants;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider for generating and validating JWT tokens
 */
@Slf4j
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.jwt.temporary-token-expiration}")
    private long temporaryExpiration;
    
    private SecretKey key;
    
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Generate access token
     */
    public String generateAccessToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.JWT_CLAIM_USER_ID, userId);
        claims.put(Constants.JWT_CLAIM_EMAIL, email);
        claims.put(Constants.JWT_CLAIM_ROLE, role);
        claims.put("type", Constants.JWT_TYPE_ACCESS);
        
        return generateToken(claims, accessTokenExpiration);
    }
    
    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.JWT_CLAIM_USER_ID, userId);
        claims.put("type", Constants.JWT_TYPE_REFRESH);
        
        return generateToken(claims, refreshTokenExpiration);
    }
    
    /**
     * Generate temporary token for password reset (5 minutes)
     */
    public String generateTemporaryToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.JWT_CLAIM_USER_ID, userId);
        claims.put(Constants.JWT_CLAIM_EMAIL, email);
        claims.put("type", Constants.JWT_TYPE_TEMPORARY);
        
        // Temporary token expires in 5 minutes
        return generateToken(claims, temporaryExpiration);
    }
    
    /**
     * Hash token for storage (using SHA-256)
     */
    public String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Generate token with claims and expiration
     */
    private String generateToken(Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * Get user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        Object userIdObj = claims.get(Constants.JWT_CLAIM_USER_ID);
        
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        
        throw new IllegalArgumentException("Invalid user ID in token");
    }
    
    /**
     * Get email from JWT token
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get(Constants.JWT_CLAIM_EMAIL, String.class);
    }
    
    /**
     * Get role from JWT token
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get(Constants.JWT_CLAIM_ROLE, String.class);
    }
    
    /**
     * Get claims from token
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
