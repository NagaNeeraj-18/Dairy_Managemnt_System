package com.dairy.milk.service;

import com.dairy.milk.dto.CreateMilkBookingRequest;
import com.dairy.milk.dto.CreateMilkSlotRequest;
import com.dairy.milk.dto.MilkBookingResponse;
import com.dairy.milk.dto.WaitingListAllocationResponse;
import com.dairy.milk.entity.MilkBooking;
import com.dairy.milk.entity.MilkSlot;
import com.dairy.milk.enums.BookingStatus;
import com.dairy.milk.enums.DeliverySlot;
import com.dairy.user.dto.CreateUserRequest;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MilkBookingFlowIntegrationTest {

    private final UserService userService;
    private final MilkSlotService milkSlotService;
    private final MilkBookingService milkBookingService;

    @Autowired
    MilkBookingFlowIntegrationTest(
            UserService userService,
            MilkSlotService milkSlotService,
            MilkBookingService milkBookingService
    ) {
        this.userService = userService;
        this.milkSlotService = milkSlotService;
        this.milkBookingService = milkBookingService;
    }

    @Test
    void cancelledMilkIsAllocatedToWaitingListAfterCancellationWindowCloses() {
        AppUser customer = createCustomer();
        MilkSlot slot = createOpenSlot(1000);

        MilkBookingResponse confirmed = milkBookingService.bookMilk(new CreateMilkBookingRequest(
                customer.getId(),
                slot.getId(),
                1000,
                null,
                "12 Test Street",
                "560001"
        ));

        MilkBookingResponse waiting = milkBookingService.bookMilk(new CreateMilkBookingRequest(
                customer.getId(),
                slot.getId(),
                1000,
                null,
                "12 Test Street",
                "560001"
        ));

        assertThat(confirmed.status()).isEqualTo(BookingStatus.CONFIRMED.name());
        assertThat(waiting.type()).isEqualTo("WAITING_LIST");

        MilkBooking cancelled = milkBookingService.cancelBooking(confirmed.id());

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.getMilkSlot().getCancelledPoolMl()).isEqualTo(1000);

        WaitingListAllocationResponse allocation = milkBookingService.closeCancellationAndAllocate(slot.getId());

        assertThat(allocation.allocatedCount()).isEqualTo(1);
        assertThat(allocation.expiredCount()).isZero();
        assertThat(allocation.remainingCancelledPoolMl()).isZero();
        assertThat(allocation.allocatedBookings()).hasSize(1);
        assertThat(allocation.allocatedBookings().get(0).status()).isEqualTo(BookingStatus.CONFIRMED.name());
    }

    private AppUser createCustomer() {
        String suffix = UUID.randomUUID().toString();
        return userService.createUser(new CreateUserRequest(
                "Test Customer",
                "customer-" + suffix + "@example.com",
                "9" + suffix.replace("-", "").substring(0, 9),
                "password123",
                UserRole.CUSTOMER
        ));
    }

    private MilkSlot createOpenSlot(int totalMilkMl) {
        LocalDateTime now = LocalDateTime.now();
        return milkSlotService.createSlot(new CreateMilkSlotRequest(
                LocalDate.now().plusDays(1),
                DeliverySlot.MORNING,
                totalMilkMl,
                now.minusHours(1),
                now.plusHours(1),
                now.plusHours(2)
        ));
    }
}
