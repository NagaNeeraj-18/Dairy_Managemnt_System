package com.dairy.milk.service;

import com.dairy.milk.dto.CreateMilkBookingRequest;
import com.dairy.milk.dto.CreateMilkSlotRequest;
import com.dairy.milk.dto.MilkBookingResponse;
import com.dairy.milk.entity.MilkSlot;
import com.dairy.milk.enums.DeliverySlot;
import com.dairy.user.dto.CreateUserRequest;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class MilkBookingOwnershipIntegrationTest {

    private final UserService userService;
    private final MilkSlotService milkSlotService;
    private final MilkBookingService milkBookingService;

    @Autowired
    MilkBookingOwnershipIntegrationTest(
            UserService userService,
            MilkSlotService milkSlotService,
            MilkBookingService milkBookingService
    ) {
        this.userService = userService;
        this.milkSlotService = milkSlotService;
        this.milkBookingService = milkBookingService;
    }

    @Test
    void bookingOwnershipCheckRejectsDifferentCustomer() {
        AppUser owner = createCustomer("owner");
        AppUser other = createCustomer("other");
        MilkSlot slot = createOpenSlot(1000);

        MilkBookingResponse booking = milkBookingService.bookMilk(new CreateMilkBookingRequest(
                owner.getId(),
                slot.getId(),
                500,
                null,
                "12 Test Street",
                "560001"
        ));

        assertThatThrownBy(() -> milkBookingService.requireBookingCustomer(booking.id(), other.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own milk booking");
    }

    private AppUser createCustomer(String prefix) {
        String suffix = UUID.randomUUID().toString();
        return userService.createUser(new CreateUserRequest(
                "Test " + prefix,
                prefix + "-" + suffix + "@example.com",
                "8" + suffix.replace("-", "").substring(0, 9),
                "password123",
                UserRole.CUSTOMER
        ));
    }

    private MilkSlot createOpenSlot(int totalMilkMl) {
        LocalDateTime now = LocalDateTime.now();
        return milkSlotService.createSlot(new CreateMilkSlotRequest(
                LocalDate.now().plusDays(2),
                DeliverySlot.EVENING,
                totalMilkMl,
                now.minusHours(1),
                now.plusHours(1),
                now.plusHours(2)
        ));
    }
}
