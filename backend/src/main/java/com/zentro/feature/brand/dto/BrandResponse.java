package com.zentro.feature.brand.dto;

import com.zentro.feature.brand.entity.Brand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for brand data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandResponse {

    private String id; // publicId
    private String name;
    private String imageUrl;
    private Boolean isFeatured;
    private Integer productsCount; // Computed on-the-fly, will be 0 for now
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create BrandResponse from Brand entity
     */
    public static BrandResponse from(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getPublicId())
                .name(brand.getName())
                .imageUrl(brand.getImageUrl())
                .isFeatured(brand.getIsFeatured())
                .productsCount(0) // Will be computed when Product entity exists
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }
}
