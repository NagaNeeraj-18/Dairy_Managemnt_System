package com.dairy.milk.controller;

import com.dairy.milk.service.MilkSlotService;

import com.dairy.milk.service.MilkBookingService;

import com.dairy.milk.dto.CreateMilkSlotRequest;
import com.dairy.milk.dto.MilkBookingResponse;
import com.dairy.milk.dto.MilkSlotResponse;
import com.dairy.milk.dto.UpdateMilkSlotQuantityRequest;
import com.dairy.milk.dto.WaitingListAllocationResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/milk-slots")
public class AdminMilkSlotController {

    private final MilkSlotService milkSlotService;
    private final MilkBookingService milkBookingService;

    public AdminMilkSlotController(MilkSlotService milkSlotService, MilkBookingService milkBookingService) {
        this.milkSlotService = milkSlotService;
        this.milkBookingService = milkBookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MilkSlotResponse createSlot(@Valid @RequestBody CreateMilkSlotRequest request) {
        return MilkSlotResponse.from(milkSlotService.createSlot(request));
    }

    @GetMapping
    public Page<MilkSlotResponse> getSlots(Pageable pageable) {
        return milkSlotService.getAllSlots(pageable)
                .map(MilkSlotResponse::from);
    }

    @PatchMapping("/{slotId}/close-booking")
    public MilkSlotResponse closeBooking(@PathVariable Long slotId) {
        return MilkSlotResponse.from(milkSlotService.closeBooking(slotId));
    }

    @PutMapping("/{slotId}/quantity")
    public MilkSlotResponse updateQuantity(@PathVariable Long slotId, @Valid @RequestBody UpdateMilkSlotQuantityRequest request) {
        return MilkSlotResponse.from(milkSlotService.updateQuantity(slotId, request.totalMilkMl()));
    }

    @PatchMapping("/{slotId}/loss")
    public MilkSlotResponse recordMilkLoss(@PathVariable Long slotId, @Valid @RequestBody com.dairy.milk.dto.RecordMilkLossRequest request) {
        return MilkSlotResponse.from(milkSlotService.recordMilkLoss(slotId, request.lostMilkMl()));
    }

    @PostMapping("/{slotId}/close-cancellation-and-allocate")
    public WaitingListAllocationResponse closeCancellationAndAllocate(@PathVariable Long slotId) {
        return milkBookingService.closeCancellationAndAllocate(slotId);
    }

    @GetMapping("/{slotId}/bookings")
    public Page<MilkBookingResponse> getSlotBookings(@PathVariable Long slotId, Pageable pageable) {
        return milkBookingService.getSlotBookings(slotId, pageable)
                .map(MilkBookingResponse::fromBooking);
    }
}
