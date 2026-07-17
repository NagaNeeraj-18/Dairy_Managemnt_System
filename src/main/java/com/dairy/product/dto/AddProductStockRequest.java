package com.dairy.product.dto;

import jakarta.validation.constraints.Min;

public record AddProductStockRequest(@Min(1) int quantityToAdd) {
}
