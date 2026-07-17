package com.dairy.feedback.dto;

import com.dairy.delivery.enums.OrderType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateFeedbackRequest(
        @NotNull Long customerId,
        @NotNull OrderType orderType,
        @NotNull Long orderId,
        @Min(1) @Max(5) int productRating,
        @Min(1) @Max(5) int deliveryBoyRating,
        String comment
) {
}
