package com.zentro.feature.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating categories
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(min = 3, max = 100, message = "Category name must be between 3 and 100 characters")
    private String name;
    
    /**
     * Public ID of parent category (optional)
     * If null, this will be a root category
     */
    private String parentId;
    
    /**
     * Whether this category should be featured.
     * Featured categories appear prominently in the UI
     */
    @Builder.Default
    private Boolean isFeatured = false;
}
