package com.dairy.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOfflineSaleRequest(
        @NotBlank String itemName,
        @Min(1) int quantity,
        @NotBlank String unitLabel,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        String customerName
) {
}
