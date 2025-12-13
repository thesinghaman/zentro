package com.zentro.feature.auth.repository;

import com.zentro.feature.auth.entity.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for RefreshToken entity
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find refresh token by token hash
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    /**
     * Delete refresh tokens by user ID
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
    void deleteByUserId(Long userId);
    
    /**
     * Delete expired refresh tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
    
    /**
     * Delete specific refresh token by token hash
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash")
    void deleteByTokenHash(String tokenHash);
}
