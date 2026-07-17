package com.dairy.milk.dto;

import java.util.List;

public record WaitingListAllocationResponse(
        Long milkSlotId,
        int allocatedCount,
        int expiredCount,
        int remainingCancelledPoolMl,
        List<MilkBookingResponse> allocatedBookings
) {
}
