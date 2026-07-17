package com.dairy.product.dto;

import com.dairy.product.entity.ProductOrder;
import com.dairy.product.enums.ProductOrderStatus;
import com.dairy.common.enums.PaymentMode;
import com.dairy.common.enums.PaymentStatus;

import java.math.BigDecimal;

public record ProductOrderResponse(
        Long id,
        Long customerId,
        Long productId,
        String productName,
        int quantity,
        BigDecimal totalAmount,
        String deliveryAddress,
        String pincode,
        ProductOrderStatus status,
        PaymentStatus paymentStatus,
        PaymentMode paymentMode
) {
    public static ProductOrderResponse from(ProductOrder order) {
        return new ProductOrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getPincode(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getPaymentMode()
        );
    }
}
