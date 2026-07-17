package com.dairy.delivery.dto;

import com.dairy.delivery.entity.DeliveryAssignment;
import com.dairy.delivery.enums.DeliveryStatus;
import com.dairy.delivery.enums.OrderType;

public record DeliveryAssignmentResponse(
        Long id,
        Long deliveryBoyId,
        OrderType orderType,
        Long orderId,
        String deliveryAddress,
        String pincode,
        DeliveryStatus status,
        String otpForTesting
) {
    public static DeliveryAssignmentResponse from(DeliveryAssignment assignment) {
        return new DeliveryAssignmentResponse(
                assignment.getId(),
                assignment.getDeliveryBoy().getId(),
                assignment.getOrderType(),
                assignment.getOrderId(),
                assignment.getDeliveryAddress(),
                assignment.getPincode(),
                assignment.getStatus(),
                assignment.getOtp()
        );
    }
}
