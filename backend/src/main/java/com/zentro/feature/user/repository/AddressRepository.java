package com.zentro.feature.user.repository;

import com.zentro.feature.user.entity.Address;
import com.zentro.feature.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Address entity
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find all addresses by user
     */
    List<Address> findByUser(User user);

    /**
     * Find all addresses by user ID
     */
    List<Address> findByUserId(Long userId);

    /**
     * Find address by user and public ID
     */
    Optional<Address> findByUserAndPublicId(User user, String publicId);

    /**
     * Find address by user ID and public ID
     */
    Optional<Address> findByUserIdAndPublicId(Long userId, String publicId);

    /**
     * Find default address for user
     */
    Optional<Address> findByUserAndIsDefaultTrue(User user);

    /**
     * Find default address by user ID
     */
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Check if user has a default address
     */
    boolean existsByUserAndIsDefaultTrue(User user);

    /**
     * Check if user ID has a default address
     */
    boolean existsByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Check if public ID exists
     */
    boolean existsByPublicId(String publicId);

    /**
     * Count addresses by user
     */
    long countByUser(User user);

    /**
     * Count addresses by user ID
     */
    long countByUserId(Long userId);

    /**
     * Set all user addresses to non-default
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void unsetDefaultForUser(Long userId);

    /**
     * Delete all addresses for a user
     */
    void deleteByUser(User user);

    /**
     * Delete all addresses by user ID
     */
    void deleteByUserId(Long userId);
}
