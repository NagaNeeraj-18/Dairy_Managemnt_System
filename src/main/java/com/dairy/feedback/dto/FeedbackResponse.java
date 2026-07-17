package com.dairy.feedback.dto;

import com.dairy.delivery.enums.OrderType;
import com.dairy.feedback.entity.Feedback;

public record FeedbackResponse(
        Long id,
        Long customerId,
        OrderType orderType,
        Long orderId,
        int productRating,
        int deliveryBoyRating,
        String comment
) {
    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getCustomer().getId(),
                feedback.getOrderType(),
                feedback.getOrderId(),
                feedback.getProductRating(),
                feedback.getDeliveryBoyRating(),
                feedback.getComment()
        );
    }
}
