package com.dairy.user.dto;

import com.dairy.user.entity.Address;

public record AddressResponse(
        Long id,
        Long userId,
        String line1,
        String line2,
        String city,
        String pincode,
        boolean defaultAddress
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getUser().getId(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getPincode(),
                address.isDefaultAddress()
        );
    }
}
