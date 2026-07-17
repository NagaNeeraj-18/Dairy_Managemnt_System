package com.dairy.milk.dto;

import com.dairy.milk.enums.BookingStatus;
import com.dairy.milk.entity.MilkBooking;
import com.dairy.milk.entity.WaitingListEntry;
import com.dairy.milk.enums.WaitingListStatus;
import com.dairy.common.enums.PaymentMode;
import com.dairy.common.enums.PaymentStatus;
import com.dairy.common.exception.InvalidOperationException;

public record MilkBookingResponse(
        Long id,
        Long customerId,
        Long milkSlotId,
        int quantityMl,
        String deliveryAddress,
        String pincode,
        String status,
        String type,
        PaymentStatus paymentStatus,
        PaymentMode paymentMode
) {
    public static MilkBookingResponse fromBooking(MilkBooking booking) {
        return new MilkBookingResponse(
                booking.getId(),
                booking.getCustomer().getId(),
                booking.getMilkSlot().getId(),
                booking.getQuantityMl(),
                booking.getDeliveryAddress(),
                booking.getPincode(),
                booking.getStatus().name(),
                "BOOKING",
                booking.getPaymentStatus(),
                booking.getPaymentMode()
        );
    }

    public static MilkBookingResponse fromWaitingList(WaitingListEntry entry) {
        WaitingListStatus status = entry.getStatus();
        return new MilkBookingResponse(
                entry.getId(),
                entry.getCustomer().getId(),
                entry.getMilkSlot().getId(),
                entry.getQuantityMl(),
                entry.getDeliveryAddress(),
                entry.getPincode(),
                status.name(),
                "WAITING_LIST",
                null,
                null
        );
    }

    public static MilkBookingResponse allocatedFromWaitingList(MilkBooking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidOperationException("Allocated booking must be confirmed");
        }
        return fromBooking(booking);
    }
}
