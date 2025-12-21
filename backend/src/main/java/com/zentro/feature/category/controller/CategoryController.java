package com.zentro.feature.category.controller;

import com.zentro.common.dto.ApiResponse;
import com.zentro.common.util.Constants;
import com.zentro.feature.category.dto.CategoryResponse;
import com.zentro.feature.category.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public controller for category operations
 * Available to all users (no authentication required)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * GET /api/v1/categories
     * Get all root categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        log.info("GET /api/v1/categories - Get root categories");
        List<CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_ROOT_CATEGORIES_RETRIEVED, categories));
    }
    
    /**
     * GET /api/v1/categories/{id}
     * Get category details by public ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable String id) {
        log.info("GET /api/v1/categories/{} - Get category details", id);
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_CATEGORY_RETRIEVED, category));
    }
    
    /**
     * GET /api/v1/categories/{id}/children
     * Get all subcategories of a parent category
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubcategories(@PathVariable String id) {
        log.info("GET /api/v1/categories/{}/children - Get subcategories", id);
        List<CategoryResponse> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_SUBCATEGORIES_RETRIEVED, subcategories));
    }
    
    /**
     * GET /api/v1/categories/featured
     * Get all featured categories
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getFeaturedCategories() {
        log.info("GET /api/v1/categories/featured - Get featured categories");
        List<CategoryResponse> categories = categoryService.getFeaturedCategories();
        return ResponseEntity.ok(ApiResponse.success(Constants.SUCCESS_FEATURED_CATEGORIES_RETRIEVED, categories));
    }
}
