package com.dairy.milk.controller;

import com.dairy.common.dto.MarkPaymentRequest;
import com.dairy.security.service.AuthenticatedUserService;
import com.dairy.milk.service.MilkBookingService;

import com.dairy.milk.dto.CreateMilkBookingRequest;
import com.dairy.milk.dto.MilkBookingResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/milk-bookings")
public class CustomerMilkBookingController {

    private final MilkBookingService milkBookingService;
    private final AuthenticatedUserService authenticatedUserService;

    public CustomerMilkBookingController(MilkBookingService milkBookingService, AuthenticatedUserService authenticatedUserService) {
        this.milkBookingService = milkBookingService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MilkBookingResponse bookMilk(@Valid @RequestBody CreateMilkBookingRequest request) {
        authenticatedUserService.requireCurrentUser(request.customerId());
        return milkBookingService.bookMilk(request);
    }

    @DeleteMapping("/{bookingId}")
    public MilkBookingResponse cancelBooking(@PathVariable Long bookingId) {
        milkBookingService.requireBookingCustomer(bookingId, authenticatedUserService.currentUserId());
        return MilkBookingResponse.fromBooking(milkBookingService.cancelBooking(bookingId));
    }

    @GetMapping("/users/{customerId}")
    public Page<MilkBookingResponse> getCustomerBookings(@PathVariable Long customerId, Pageable pageable) {
        authenticatedUserService.requireCurrentUser(customerId);
        return milkBookingService.getCustomerBookings(customerId, pageable)
                .map(MilkBookingResponse::fromBooking);
    }

    @PostMapping("/{bookingId}/payment")
    public MilkBookingResponse markPaid(@PathVariable Long bookingId, @Valid @RequestBody MarkPaymentRequest request) {
        milkBookingService.requireBookingCustomer(bookingId, authenticatedUserService.currentUserId());
        return MilkBookingResponse.fromBooking(milkBookingService.markPaid(bookingId, request.paymentMode()));
    }
}
