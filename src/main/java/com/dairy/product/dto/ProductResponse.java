package com.dairy.product.dto;

import com.dairy.product.entity.Product;
import com.dairy.product.enums.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String unitLabel,
        BigDecimal price,
        int stockQuantity,
        ProductStatus status
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getUnitLabel(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus()
        );
    }
}
