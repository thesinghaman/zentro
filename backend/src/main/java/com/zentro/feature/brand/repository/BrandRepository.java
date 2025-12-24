package com.zentro.feature.brand.repository;

import com.zentro.feature.brand.entity.Brand;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Brand entity
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Find brand by public ID
     */
    Optional<Brand> findByPublicId(String publicId);

    /**
     * Check if brand name exists globally
     */
    boolean existsByName(String name);

    /**
     * Check if brand name exists excluding the given ID (for updates)
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Get all featured brands
     */
    List<Brand> findByIsFeaturedTrue();

    /**
     * Get all brands ordered by name alphabetically
     */
    List<Brand> findAllByOrderByNameAsc();
}
