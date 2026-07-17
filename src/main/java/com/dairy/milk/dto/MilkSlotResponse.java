package com.dairy.milk.dto;

import com.dairy.milk.enums.DeliverySlot;
import com.dairy.milk.entity.MilkSlot;
import com.dairy.milk.enums.WindowStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MilkSlotResponse(
        Long id,
        LocalDate deliveryDate,
        DeliverySlot deliverySlot,
        WindowStatus bookingStatus,
        WindowStatus cancellationStatus,
        int totalMilkMl,
        int availableMilkMl,
        int cancelledPoolMl,
        int lostMilkMl,
        LocalDateTime bookingOpensAt,
        LocalDateTime bookingClosesAt,
        LocalDateTime cancellationClosesAt
) {
    public static MilkSlotResponse from(MilkSlot slot) {
        return new MilkSlotResponse(
                slot.getId(),
                slot.getDeliveryDate(),
                slot.getDeliverySlot(),
                slot.getBookingStatus(),
                slot.getCancellationStatus(),
                slot.getTotalMilkMl(),
                slot.getAvailableMilkMl(),
                slot.getCancelledPoolMl(),
                slot.getLostMilkMl(),
                slot.getBookingOpensAt(),
                slot.getBookingClosesAt(),
                slot.getCancellationClosesAt()
        );
    }
}
