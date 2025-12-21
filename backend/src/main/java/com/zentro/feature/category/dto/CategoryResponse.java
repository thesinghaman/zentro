package com.zentro.feature.category.dto;

import com.zentro.feature.category.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for category data
 * Includes parent info for breadcrumbs and children count for UI indicators
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    
    private String id; // Public ID (e.g., CAT-1733707200-A7X9F2)
    private String name;
    private String imageUrl;
    private Boolean isFeatured;
    
    /**
     * Parent category info for breadcrumbs
     */
    private String parentId;
    private String parentName;
    
    /**
     * Number of child categories
     * Useful for UI to show expandable categories
     */
    private Integer childrenCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method to create CategoryResponse from Category entity
     */
    public static CategoryResponse from(Category category) {
        CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getPublicId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .isFeatured(category.getIsFeatured())
                .childrenCount(category.getChildrenCount())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt());
        
        // Add parent info if exists
        if (category.getParent() != null) {
            builder.parentId(category.getParent().getPublicId())
                   .parentName(category.getParent().getName());
        }
        
        return builder.build();
    }
}
