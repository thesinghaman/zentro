package com.zentro.feature.brand.service;

import com.zentro.feature.brand.dto.BrandRequest;
import com.zentro.feature.brand.dto.BrandResponse;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for brand management operations
 */
public interface BrandService {

    /**
     * Get all brands ordered alphabetically
     */
    List<BrandResponse> getAllBrands();

    /**
     * Get brand by public ID
     */
    BrandResponse getBrandById(String publicId);

    /**
     * Get all featured brands
     */
    List<BrandResponse> getFeaturedBrands();

    /**
     * Create a new brand (ADMIN only)
     */
    BrandResponse createBrand(BrandRequest request, MultipartFile image);

    /**
     * Update an existing brand (ADMIN only)
     */
    BrandResponse updateBrand(String publicId, BrandRequest request, MultipartFile image);

    /**
     * Delete a brand (ADMIN only)
     */
    void deleteBrand(String publicId);
}
