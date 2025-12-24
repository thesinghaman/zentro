package com.zentro.feature.brand.controller;

import com.zentro.common.dto.ApiResponse;
import com.zentro.common.util.Constants;
import com.zentro.feature.brand.dto.BrandRequest;
import com.zentro.feature.brand.dto.BrandResponse;
import com.zentro.feature.brand.service.BrandService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin REST controller for brand management
 * All endpoints require ADMIN role
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/brands")
@RequiredArgsConstructor
public class AdminBrandController {

    private final BrandService brandService;

    /**
     * Create a new brand
     * Requires ADMIN role
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @ModelAttribute BrandRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("POST /api/v1/admin/brands - Create brand: {}", request.getName());
        BrandResponse brand = brandService.createBrand(request, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Constants.SUCCESS_BRAND_CREATED, brand));
    }

    /**
     * Update an existing brand
     * Requires ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable String id,
            @Valid @ModelAttribute BrandRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("PUT /api/v1/admin/brands/{} - Update brand: {}", id, request.getName());
        BrandResponse brand = brandService.updateBrand(id, request, image);
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_BRAND_UPDATED, brand));
    }

    /**
     * Delete a brand
     * Requires ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable String id) {
        log.info("DELETE /api/v1/admin/brands/{} - Delete brand", id);
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_BRAND_DELETED));
    }
}
