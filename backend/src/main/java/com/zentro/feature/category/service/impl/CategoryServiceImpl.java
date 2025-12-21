package com.zentro.feature.category.service.impl;

import com.zentro.common.exception.BadRequestException;
import com.zentro.common.exception.ResourceNotFoundException;
import com.zentro.common.service.R2StorageService;
import com.zentro.common.util.Constants;
import com.zentro.common.util.PublicIdGenerator;
import com.zentro.feature.category.dto.CategoryRequest;
import com.zentro.feature.category.dto.CategoryResponse;
import com.zentro.feature.category.entity.Category;
import com.zentro.feature.category.repository.CategoryRepository;
import com.zentro.feature.category.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of category service
 * Handles category CRUD operations and hierarchy management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final R2StorageService r2StorageService;
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        log.info("Fetching all root categories");
        List<Category> categories = categoryRepository.findByParentIsNull();
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String publicId) {
        log.info("Fetching category by publicId: {}", publicId);
        Category category = categoryRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_CATEGORY_NOT_FOUND));
        return CategoryResponse.from(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(String parentPublicId) {
        log.info("Fetching subcategories for parent: {}", parentPublicId);
        
        // Verify parent exists
        Category parent = categoryRepository.findByPublicId(parentPublicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_CATEGORY_NOT_FOUND));
        
        // Get children
        List<Category> children = categoryRepository.findByParent(parent);
        return children.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getFeaturedCategories() {
        log.info("Fetching all featured categories");
        List<Category> categories = categoryRepository.findByIsFeaturedTrue();
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, MultipartFile imageFile) {
        log.info("Creating new category with name: {}", request.getName());
        
        // Validate parent if provided
        Category parent = null;
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            parent = categoryRepository.findByPublicId(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_INVALID_PARENT_CATEGORY));
        }
        
        // Check sibling name uniqueness
        validateNameUniqueness(request.getName(), parent);
        
        // Upload image to R2 if provided
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = r2StorageService.uploadFile(imageFile, Constants.R2_FOLDER_CATEGORIES);
            log.info("Category image uploaded: {}", imageUrl);
        }
        
        // Create category
        Category category = Category.builder()
                .publicId(PublicIdGenerator.generate(Constants.PREFIX_CATEGORY))
                .name(request.getName())
                .imageUrl(imageUrl)
                .isFeatured(request.getIsFeatured())
                .parent(parent)
                .build();
        
        category = categoryRepository.save(category);
        log.info("Category created with publicId: {}", category.getPublicId());
        
        return CategoryResponse.from(category);
    }
    
    @Override
    @Transactional
    public CategoryResponse updateCategory(String publicId, CategoryRequest request, MultipartFile imageFile) {
        log.info("Updating category: {}", publicId);
        
        // Find existing category
        Category category = categoryRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_CATEGORY_NOT_FOUND));
        
        // Validate parent if provided
        Category newParent = null;
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            newParent = categoryRepository.findByPublicId(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_INVALID_PARENT_CATEGORY));
            
            // Prevent self-reference
            if (newParent.getId().equals(category.getId())) {
                throw new BadRequestException(Constants.ERROR_CATEGORY_SELF_REFERENCE);
            }
        }
        
        // Check name uniqueness if name or parent changed
        boolean nameChanged = !category.getName().equals(request.getName());
        boolean parentChanged = (category.getParent() == null && newParent != null) ||
                                (category.getParent() != null && !category.getParent().equals(newParent));
        
        if (nameChanged || parentChanged) {
            validateNameUniqueness(request.getName(), newParent, category.getId());
        }
        
        // Upload new image if provided and delete old one
        if (imageFile != null && !imageFile.isEmpty()) {
            String oldImageUrl = category.getImageUrl();
            String newImageUrl = r2StorageService.uploadFile(imageFile, Constants.R2_FOLDER_CATEGORIES);
            category.setImageUrl(newImageUrl);
            log.info("Category image updated: {}", newImageUrl);
            
            // Delete old image from R2
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                r2StorageService.deleteFile(oldImageUrl);
                log.info("Old category image deleted: {}", oldImageUrl);
            }
        }
        
        // Update fields
        category.setName(request.getName());
        category.setIsFeatured(request.getIsFeatured());
        category.setParent(newParent);
        
        category = categoryRepository.save(category);
        log.info("Category updated: {}", publicId);
        
        return CategoryResponse.from(category);
    }
    
    @Override
    @Transactional
    public void deleteCategory(String publicId) {
        log.info("Deleting category: {}", publicId);
        
        Category category = categoryRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_CATEGORY_NOT_FOUND));
        
        // Check if category has children
        long childrenCount = categoryRepository.countByParent(category);
        if (childrenCount > 0) {
            throw new BadRequestException(Constants.ERROR_CATEGORY_HAS_CHILDREN);
        }
        
        // TODO: Check if category has products (implement when Product entity exists)
        
        // Delete image from R2 if exists
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            r2StorageService.deleteFile(category.getImageUrl());
            log.info("Category image deleted from R2: {}", category.getImageUrl());
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted: {}", publicId);
    }
    
    /**
     * Validate that category name is unique among siblings
     * Categories with the same parent cannot have duplicate names
     */
    private void validateNameUniqueness(String name, Category parent) {
        boolean exists;
        if (parent == null) {
            exists = categoryRepository.existsByNameAndParentIsNull(name);
        } else {
            exists = categoryRepository.existsByNameAndParent(name, parent);
        }
        
        if (exists) {
            throw new BadRequestException(Constants.ERROR_CATEGORY_NAME_EXISTS);
        }
    }
    
    /**
     * Validate name uniqueness excluding the current category (for updates)
     */
    private void validateNameUniqueness(String name, Category parent, Long excludeCategoryId) {
        boolean exists;
        if (parent == null) {
            exists = categoryRepository.findByParentIsNull().stream()
                    .anyMatch(c -> c.getName().equals(name) && !c.getId().equals(excludeCategoryId));
        } else {
            exists = categoryRepository.findByParent(parent).stream()
                    .anyMatch(c -> c.getName().equals(name) && !c.getId().equals(excludeCategoryId));
        }
        
        if (exists) {
            throw new BadRequestException(Constants.ERROR_CATEGORY_NAME_EXISTS);
        }
    }
}
