package com.dairy.milk.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMilkBookingRequest(
        @NotNull Long customerId,
        @NotNull Long milkSlotId,
        @Min(500) int quantityMl,
        Long addressId,
        String deliveryAddress,
        String pincode
) {
}
