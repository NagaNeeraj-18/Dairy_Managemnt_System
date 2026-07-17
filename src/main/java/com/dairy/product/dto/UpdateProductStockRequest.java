package com.dairy.product.dto;

import jakarta.validation.constraints.Min;

public record UpdateProductStockRequest(@Min(0) int stockQuantity) {
}
