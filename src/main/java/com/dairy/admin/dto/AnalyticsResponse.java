package com.dairy.admin.dto;

import java.math.BigDecimal;

public record AnalyticsResponse(
        long deliveredMilkOrders,
        long deliveredProductOrders,
        BigDecimal productRevenue,
        BigDecimal offlineRevenue,
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal netAmount,
        double averageProductRating,
        double averageDeliveryBoyRating,
        int totalLostMilkMl,
        double milkLossRate,
        double customerRetentionRate
) {
}
