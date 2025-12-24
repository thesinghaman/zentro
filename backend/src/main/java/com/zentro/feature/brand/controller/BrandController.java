package com.zentro.feature.brand.controller;

import com.zentro.common.dto.ApiResponse;
import com.zentro.common.util.Constants;
import com.zentro.feature.brand.dto.BrandResponse;
import com.zentro.feature.brand.service.BrandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public REST controller for brand operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * Get all brands (alphabetically sorted)
     * Public endpoint - no authentication required
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        log.info("GET /api/v1/brands - Get all brands");
        List<BrandResponse> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_BRANDS_RETRIEVED, brands));
    }

    /**
     * Get brand by ID
     * Public endpoint - no authentication required
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(@PathVariable String id) {
        log.info("GET /api/v1/brands/{} - Get brand by ID", id);
        BrandResponse brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_BRAND_RETRIEVED, brand));
    }

    /**
     * Get featured brands
     * Public endpoint - no authentication required
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getFeaturedBrands() {
        log.info("GET /api/v1/brands/featured - Get featured brands");
        List<BrandResponse> brands = brandService.getFeaturedBrands();
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_FEATURED_BRANDS_RETRIEVED, brands));
    }
}
