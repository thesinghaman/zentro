package com.zentro.feature.brand.service.impl;

import com.zentro.common.exception.BadRequestException;
import com.zentro.common.exception.ResourceNotFoundException;
import com.zentro.common.service.R2StorageService;
import com.zentro.common.util.Constants;
import com.zentro.common.util.PublicIdGenerator;
import com.zentro.feature.brand.dto.BrandRequest;
import com.zentro.feature.brand.dto.BrandResponse;
import com.zentro.feature.brand.entity.Brand;
import com.zentro.feature.brand.repository.BrandRepository;
import com.zentro.feature.brand.service.BrandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of BrandService with business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final R2StorageService r2StorageService;

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        log.info("Fetching all brands ordered alphabetically");
        return brandRepository.findAllByOrderByNameAsc()
                .stream()
                .map(BrandResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(String publicId) {
        log.info("Fetching brand with publicId: {}", publicId);
        Brand brand = findBrandByPublicId(publicId);

        return BrandResponse.from(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getFeaturedBrands() {
        log.info("Fetching featured brands");
        return brandRepository.findByIsFeaturedTrue()
                .stream()
                .map(BrandResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public BrandResponse createBrand(BrandRequest request, MultipartFile image) {
        log.info("Creating new brand with name: {}", request.getName());

        // Check global name uniqueness
        if (brandRepository.existsByName(request.getName())) {
            throw new BadRequestException(Constants.ERROR_BRAND_NAME_EXISTS);
        }

        Brand brand = new Brand();
        brand.setPublicId(PublicIdGenerator.generate(Constants.PREFIX_BRAND_PUBLIC_ID));
        brand.setName(request.getName());
        brand.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String imageUrl = r2StorageService.uploadFile(image, Constants.R2_FOLDER_BRANDS);
            brand.setImageUrl(imageUrl);
        }

        Brand savedBrand = brandRepository.save(brand);
        log.info("Brand created successfully with id: {}", savedBrand.getPublicId());

        return BrandResponse.from(savedBrand);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(String publicId, BrandRequest request, MultipartFile image) {
        log.info("Updating brand with publicId: {}", publicId);

        Brand brand = findBrandByPublicId(publicId);

        // Check name uniqueness excluding current brand
        if (!brand.getName().equals(request.getName()) &&
                brandRepository.existsByNameAndIdNot(request.getName(), brand.getId())) {
            throw new BadRequestException(Constants.ERROR_BRAND_NAME_EXISTS);
        }

        // Update fields
        brand.setName(request.getName());
        brand.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);

        // Handle image update
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (brand.getImageUrl() != null && !brand.getImageUrl().isEmpty()) {
                r2StorageService.deleteFile(brand.getImageUrl());
            }
            String imageUrl = r2StorageService.uploadFile(image, Constants.R2_FOLDER_BRANDS);
            brand.setImageUrl(imageUrl);
        }

        Brand updatedBrand = brandRepository.save(brand);
        log.info("Brand updated successfully: {}", updatedBrand.getPublicId());

        return BrandResponse.from(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(String publicId) {
        log.info("Deleting brand with publicId: {}", publicId);

        Brand brand = findBrandByPublicId(publicId);

        // TODO: Check if brand has products when Product entity is implemented
        // if (brand.getProducts() != null && !brand.getProducts().isEmpty()) {
        //     throw new BadRequestException(Constants.ERROR_BRAND_HAS_PRODUCTS);
        // }

        // Delete image if exists
        if (brand.getImageUrl() != null && !brand.getImageUrl().isEmpty()) {
            r2StorageService.deleteFile(brand.getImageUrl());
        }

        brandRepository.delete(brand);
        log.info("Brand deleted successfully: {}", publicId);
    }

    /**
     * Helper method to find brand by public ID
     */
    private Brand findBrandByPublicId(String publicId) {
        return brandRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_BRAND_NOT_FOUND));
    }
}
