package com.zentro.feature.category.service;

import com.zentro.feature.category.dto.CategoryRequest;
import com.zentro.feature.category.dto.CategoryResponse;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for category operations
 */
public interface CategoryService {
    
    /**
     * Get all root categories (categories without parent)
     * 
     * @return List of root categories
     */
    List<CategoryResponse> getRootCategories();
    
    /**
     * Get category by public ID
     * 
     * @param publicId Category public ID
     * @return Category details
     */
    CategoryResponse getCategoryById(String publicId);
    
    /**
     * Get all subcategories of a parent category
     * 
     * @param parentPublicId Parent category public ID
     * @return List of child categories
     */
    List<CategoryResponse> getSubcategories(String parentPublicId);
    
    /**
     * Get all featured categories
     * 
     * @return List of featured categories
     */
    List<CategoryResponse> getFeaturedCategories();
    
    /**
     * Create a new category (ADMIN only)
     * 
     * @param request Category creation request
     * @param imageFile Optional image file to upload
     * @return Created category
     */
    CategoryResponse createCategory(CategoryRequest request, MultipartFile imageFile);
    
    /**
     * Update an existing category (ADMIN only)
     * 
     * @param publicId Category public ID
     * @param request Category update request
     * @param imageFile Optional image file to upload (replaces existing image)
     * @return Updated category
     */
    CategoryResponse updateCategory(String publicId, CategoryRequest request, MultipartFile imageFile);
    
    /**
     * Delete a category (ADMIN only)
     * Cannot delete if category has children or products
     * 
     * @param publicId Category public ID
     */
    void deleteCategory(String publicId);
}
