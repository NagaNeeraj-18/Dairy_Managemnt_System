package com.dairy.milk.dto;

import com.dairy.milk.enums.DeliverySlot;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateMilkSlotRequest(
        @NotNull LocalDate deliveryDate,
        @NotNull DeliverySlot deliverySlot,
        @Min(500) int totalMilkMl,
        LocalDateTime bookingOpensAt,
        LocalDateTime bookingClosesAt,
        LocalDateTime cancellationClosesAt
) {
}
