package com.zentro.feature.user.service;

import com.zentro.feature.user.dto.request.AddressRequest;
import com.zentro.feature.user.dto.response.AddressResponse;

import java.util.List;

/**
 * Service interface for address management
 */
public interface AddressService {

    /**
     * Get all addresses for the authenticated user
     *
     * @param userId User ID
     * @return List of addresses
     */
    List<AddressResponse> getAllAddresses(Long userId);

    /**
     * Get address by ID for the authenticated user
     *
     * @param userId    User ID
     * @param publicId  Address public ID
     * @return Address details
     */
    AddressResponse getAddressById(Long userId, String publicId);

    /**
     * Add new address for the authenticated user
     *
     * @param userId  User ID
     * @param request Address request data
     * @return Created address
     */
    AddressResponse addAddress(Long userId, AddressRequest request);

    /**
     * Update existing address
     *
     * @param userId    User ID
     * @param publicId  Address public ID
     * @param request   Updated address data
     * @return Updated address
     */
    AddressResponse updateAddress(Long userId, String publicId, AddressRequest request);

    /**
     * Delete address
     *
     * @param userId    User ID
     * @param publicId  Address public ID
     */
    void deleteAddress(Long userId, String publicId);

    /**
     * Set address as default
     *
     * @param userId    User ID
     * @param publicId  Address public ID
     * @return Updated address
     */
    AddressResponse setDefaultAddress(Long userId, String publicId);
}
