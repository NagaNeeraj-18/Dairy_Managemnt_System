package com.dairy.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductOrderRequest(
        @NotNull Long customerId,
        @NotNull Long productId,
        @Min(1) int quantity,
        Long addressId,
        String deliveryAddress,
        String pincode
) {
}
