package com.zentro.feature.user.service.impl;

import com.zentro.common.exception.ResourceNotFoundException;
import com.zentro.common.util.Constants;
import com.zentro.common.util.PublicIdGenerator;
import com.zentro.feature.user.dto.request.AddressRequest;
import com.zentro.feature.user.dto.response.AddressResponse;
import com.zentro.feature.user.entity.Address;
import com.zentro.feature.user.entity.User;
import com.zentro.feature.user.repository.AddressRepository;
import com.zentro.feature.user.repository.UserRepository;
import com.zentro.feature.user.service.AddressService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AddressService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getAllAddresses(Long userId) {
        log.info("Fetching all addresses for user ID: {}", userId);
        
        List<Address> addresses = addressRepository.findByUserId(userId);
        
        return addresses.stream()
                .map(AddressResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse getAddressById(Long userId, String publicId) {
        log.info("Fetching address with public ID: {} for user ID: {}", publicId, userId);
        
        Address address = addressRepository.findByUserIdAndPublicId(userId, publicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_ADDRESS_NOT_FOUND));
        
        return AddressResponse.from(address);
    }

    @Override
    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        log.info("Adding new address for user ID: {}", userId);
        
        // Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));
        
        // Check if this is user's first address - auto-default
        boolean isFirstAddress = addressRepository.countByUserId(userId) == 0;
        
        // Create new address
        Address address = Address.builder()
                .publicId(PublicIdGenerator.generate("ADR"))
                .user(user)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .addressType(request.getAddressType())
                .isDefault(isFirstAddress)
                .build();
        
        address = addressRepository.save(address);
        log.info("Address created with public ID: {} (isDefault: {})", address.getPublicId(), isFirstAddress);
        
        return AddressResponse.from(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, String publicId, AddressRequest request) {
        log.info("Updating address with public ID: {} for user ID: {}", publicId, userId);
        
        // Fetch address and verify ownership
        Address address = addressRepository.findByUserIdAndPublicId(userId, publicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_ADDRESS_NOT_FOUND));
        
        // Update address fields only - isDefault is NOT updated here
        address.setName(request.getName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setAddressType(request.getAddressType());
        // isDefault is intentionally NOT updated - use /select endpoint to change default
        
        address = addressRepository.save(address);
        log.info("Address updated: {}", publicId);
        
        return AddressResponse.from(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, String publicId) {
        log.info("Deleting address with public ID: {} for user ID: {}", publicId, userId);
        
        // Fetch address and verify ownership
        Address address = addressRepository.findByUserIdAndPublicId(userId, publicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_ADDRESS_NOT_FOUND));
        
        boolean wasDefault = address.getIsDefault();
        
        // Delete the address
        addressRepository.delete(address);
        log.info("Address deleted: {}", publicId);
        
        // If deleted address was default, auto-assign new default to first remaining address
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.getFirst();
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("Auto-assigned new default address: {}", newDefault.getPublicId());
            }
        }
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long userId, String publicId) {
        log.info("Setting default address to: {} for user ID: {}", publicId, userId);
        
        // Fetch address and verify ownership
        Address address = addressRepository.findByUserIdAndPublicId(userId, publicId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_ADDRESS_NOT_FOUND));
        
        // Unset all other default addresses for this user
        addressRepository.unsetDefaultForUser(userId);
        
        // Set this address as default
        address.setIsDefault(true);
        address = addressRepository.save(address);
        
        log.info("Default address set to: {}", publicId);
        
        return AddressResponse.from(address);
    }
}
