package com.dairy.admin.dto;

import com.dairy.admin.entity.OfflineSale;

import java.math.BigDecimal;

public record OfflineSaleResponse(Long id, String itemName, int quantity, String unitLabel, BigDecimal amount, String customerName) {
    public static OfflineSaleResponse from(OfflineSale sale) {
        return new OfflineSaleResponse(sale.getId(), sale.getItemName(), sale.getQuantity(), sale.getUnitLabel(), sale.getAmount(), sale.getCustomerName());
    }
}
