package com.zentro.feature.category.repository;

import com.zentro.feature.category.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity
 * Provides query methods for category hierarchy navigation and validation
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by public ID
     */
    Optional<Category> findByPublicId(String publicId);
    
    /**
     * Find all root categories (categories without parent)
     */
    List<Category> findByParentIsNull();
    
    /**
     * Find all children of a parent category
     */
    List<Category> findByParent(Category parent);
    
    /**
     * Find all featured categories
     */
    List<Category> findByIsFeaturedTrue();
    
    /**
     * Check if category name exists under same parent (sibling uniqueness)
     * Used for validation when creating/updating categories
     */
    boolean existsByNameAndParent(String name, Category parent);
    
    /**
     * Check if category name exists as root category
     */
    boolean existsByNameAndParentIsNull(String name);
    
    /**
     * Count children of a parent category
     * Used to check if category can be deleted
     */
    long countByParent(Category parent);
}
