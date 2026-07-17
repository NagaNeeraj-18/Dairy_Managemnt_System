package com.dairy.milk.entity;

import com.dairy.milk.enums.DeliverySlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MilkSlotTest {

    @Test
    void updateTotalMilkKeepsAlreadyBookedMilkAndRecalculatesAvailableMilk() {
        MilkSlot slot = newSlot(2000);
        slot.reduceAvailable(1500);

        slot.updateTotalMilk(2500);

        assertThat(slot.getTotalMilkMl()).isEqualTo(2500);
        assertThat(slot.getAvailableMilkMl()).isEqualTo(1000);
    }

    @Test
    void updateTotalMilkCannotGoBelowAlreadyBookedMilk() {
        MilkSlot slot = newSlot(2000);
        slot.reduceAvailable(1500);

        assertThatThrownBy(() -> slot.updateTotalMilk(1000))
                .isInstanceOf(com.dairy.common.exception.InvalidOperationException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void bookingWindowMustBeOpenAndInsideTimeRange() {
        LocalDateTime now = LocalDateTime.now();
        MilkSlot slot = new MilkSlot(
                LocalDate.now().plusDays(1),
                DeliverySlot.MORNING,
                1000,
                now.minusMinutes(10),
                now.plusMinutes(10),
                now.plusMinutes(20)
        );

        assertThat(slot.isBookingOpenAt(now)).isTrue();

        slot.closeBooking();

        assertThat(slot.isBookingOpenAt(now)).isFalse();
    }

    private MilkSlot newSlot(int totalMilkMl) {
        LocalDateTime now = LocalDateTime.now();
        return new MilkSlot(
                LocalDate.now().plusDays(1),
                DeliverySlot.MORNING,
                totalMilkMl,
                now.minusHours(1),
                now.plusHours(1),
                now.plusHours(2)
        );
    }
}
