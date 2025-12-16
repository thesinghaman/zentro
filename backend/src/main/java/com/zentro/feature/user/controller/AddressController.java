package com.zentro.feature.user.controller;

import com.zentro.common.dto.ApiResponse;
import com.zentro.common.security.UserPrincipal;
import com.zentro.common.util.Constants;
import com.zentro.feature.user.dto.request.AddressRequest;
import com.zentro.feature.user.dto.response.AddressResponse;
import com.zentro.feature.user.service.AddressService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for address management endpoints
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_VERSION + "/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * Get all addresses for authenticated user
     * GET /api/v1/addresses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("GET /api/v1/addresses - User ID: {}", userPrincipal.getId());
        
        List<AddressResponse> addresses = addressService.getAllAddresses(userPrincipal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<List<AddressResponse>>builder()
                        .success(true)
                        .data(addresses)
                        .build()
        );
    }

    /**
     * Get address by ID
     * GET /api/v1/addresses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("GET /api/v1/addresses/{} - User ID: {}", id, userPrincipal.getId());
        
        AddressResponse address = addressService.getAddressById(userPrincipal.getId(), id);
        
        return ResponseEntity.ok(
                ApiResponse.<AddressResponse>builder()
                        .success(true)
                        .data(address)
                        .build()
        );
    }

    /**
     * Add new address
     * POST /api/v1/addresses
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("POST /api/v1/addresses - User ID: {}", userPrincipal.getId());
        
        AddressResponse address = addressService.addAddress(userPrincipal.getId(), request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<AddressResponse>builder()
                        .success(true)
                        .message(Constants.SUCCESS_ADDRESS_ADDED)
                        .data(address)
                        .build()
        );
    }

    /**
     * Update address
     * PUT /api/v1/addresses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable String id,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("PUT /api/v1/addresses/{} - User ID: {}", id, userPrincipal.getId());
        
        AddressResponse address = addressService.updateAddress(userPrincipal.getId(), id, request);
        
        return ResponseEntity.ok(
                ApiResponse.<AddressResponse>builder()
                        .success(true)
                        .message(Constants.SUCCESS_ADDRESS_UPDATED)
                        .data(address)
                        .build()
        );
    }

    /**
     * Delete address
     * DELETE /api/v1/addresses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("DELETE /api/v1/addresses/{} - User ID: {}", id, userPrincipal.getId());
        
        addressService.deleteAddress(userPrincipal.getId(), id);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(Constants.SUCCESS_ADDRESS_DELETED)
                        .build()
        );
    }

    /**
     * Set address as default
     * PUT /api/v1/addresses/{id}/select
     */
    @PutMapping("/{id}/select")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("PUT /api/v1/addresses/{}/select - User ID: {}", id, userPrincipal.getId());
        
        AddressResponse address = addressService.setDefaultAddress(userPrincipal.getId(), id);
        
        return ResponseEntity.ok(
                ApiResponse.<AddressResponse>builder()
                        .success(true)
                        .message(Constants.SUCCESS_DEFAULT_ADDRESS_SET)
                        .data(address)
                        .build()
        );
    }
}
