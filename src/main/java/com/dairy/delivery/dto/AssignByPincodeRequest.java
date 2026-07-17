package com.dairy.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignByPincodeRequest(
        @NotNull Long deliveryBoyId,
        @NotBlank String pincode
) {
}
