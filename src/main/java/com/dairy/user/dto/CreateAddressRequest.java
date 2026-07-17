package com.dairy.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAddressRequest(
        @NotNull Long userId,
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        @NotBlank String pincode,
        boolean defaultAddress
) {
}
