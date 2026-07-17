package com.dairy.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductPriceRequest(@NotNull @DecimalMin("0.01") BigDecimal price) {
}
