package com.dairy.user.controller;

import com.dairy.user.dto.AddressResponse;
import com.dairy.user.dto.CreateAddressRequest;
import com.dairy.security.service.AuthenticatedUserService;
import com.dairy.user.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/addresses")
public class AddressController {

    private final AddressService addressService;
    private final AuthenticatedUserService authenticatedUserService;

    public AddressController(AddressService addressService, AuthenticatedUserService authenticatedUserService) {
        this.addressService = addressService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse createAddress(@Valid @RequestBody CreateAddressRequest request) {
        authenticatedUserService.requireCurrentUser(request.userId());
        return AddressResponse.from(addressService.createAddress(request));
    }

    @GetMapping("/users/{userId}")
    public List<AddressResponse> getUserAddresses(@PathVariable Long userId) {
        authenticatedUserService.requireCurrentUser(userId);
        return addressService.getUserAddresses(userId).stream().map(AddressResponse::from).toList();
    }

    @PatchMapping("/users/{userId}/{addressId}/default")
    public AddressResponse markDefault(@PathVariable Long userId, @PathVariable Long addressId) {
        authenticatedUserService.requireCurrentUser(userId);
        return AddressResponse.from(addressService.markDefault(userId, addressId));
    }
}
