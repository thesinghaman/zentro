package com.zentro.feature.category.controller;

import com.zentro.common.dto.ApiResponse;
import com.zentro.common.util.Constants;
import com.zentro.feature.category.dto.CategoryRequest;
import com.zentro.feature.category.dto.CategoryResponse;
import com.zentro.feature.category.service.CategoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin controller for category management
 * All endpoints require ADMIN role
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * POST /api/v1/admin/categories
     * Create a new category
     * Requires ADMIN role
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @ModelAttribute CategoryRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        log.info("POST /api/v1/admin/categories - Create category: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request, imageFile);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(Constants.SUCCESS_CATEGORY_CREATED, response));
    }
    
    /**
     * PUT /api/v1/admin/categories/{id}
     * Update an existing category
     * Requires ADMIN role
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String id,
            @Valid @ModelAttribute CategoryRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        log.info("PUT /api/v1/admin/categories/{} - Update category", id);
        CategoryResponse response = categoryService.updateCategory(id, request, imageFile);
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_CATEGORY_UPDATED, response));
    }
    
    /**
     * DELETE /api/v1/admin/categories/{id}
     * Delete a category
     * Requires ADMIN role
     * Cannot delete if category has children or products
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        log.info("DELETE /api/v1/admin/categories/{} - Delete category", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_CATEGORY_DELETED, null));
    }
}
