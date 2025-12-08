package com.zentro.feature.auth.repository;

import com.zentro.feature.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email (excluding deleted users)
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    /**
     * Find user by username (excluding deleted users)
     */
    Optional<User> findByUsernameAndIsDeletedFalse(String username);
    
    /**
     * Find user by email (including deleted users)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if email exists (excluding deleted users)
     */
    boolean existsByEmailAndIsDeletedFalse(String email);
    
    /**
     * Check if username exists (excluding deleted users)
     */
    boolean existsByUsernameAndIsDeletedFalse(String username);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Find user by ID (excluding deleted users)
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findActiveById(Long id);
}
