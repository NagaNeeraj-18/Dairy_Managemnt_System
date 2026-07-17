package com.dairy.milk.service;

import com.dairy.common.enums.PaymentMode;
import com.dairy.notification.service.NotificationService;
import com.dairy.user.entity.Address;
import com.dairy.user.service.AddressService;
import com.dairy.milk.repository.WaitingListRepository;
import com.dairy.milk.repository.MilkBookingRepository;
import com.dairy.milk.enums.WindowStatus;
import com.dairy.milk.enums.WaitingListStatus;
import com.dairy.milk.enums.MilkQuantity;
import com.dairy.milk.enums.BookingStatus;
import com.dairy.milk.entity.WaitingListEntry;
import com.dairy.milk.entity.MilkSlot;
import com.dairy.milk.entity.MilkBooking;
import com.dairy.milk.dto.CreateMilkBookingRequest;
import com.dairy.milk.dto.MilkBookingResponse;
import com.dairy.milk.dto.WaitingListAllocationResponse;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.service.UserService;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.common.exception.BookingWindowClosedException;
import com.dairy.common.exception.InvalidRoleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MilkBookingService {

    private static final Logger log = LoggerFactory.getLogger(MilkBookingService.class);
    private final MilkSlotService milkSlotService;
    private final UserService userService;
    private final AddressService addressService;
    private final NotificationService notificationService;
    private final MilkBookingRepository milkBookingRepository;
    private final WaitingListRepository waitingListRepository;

    public MilkBookingService(
            MilkSlotService milkSlotService,
            UserService userService,
            AddressService addressService,
            NotificationService notificationService,
            MilkBookingRepository milkBookingRepository,
            WaitingListRepository waitingListRepository
    ) {
        this.milkSlotService = milkSlotService;
        this.userService = userService;
        this.addressService = addressService;
        this.notificationService = notificationService;
        this.milkBookingRepository = milkBookingRepository;
        this.waitingListRepository = waitingListRepository;
    }

    @Transactional
    public MilkBookingResponse bookMilk(CreateMilkBookingRequest request) {
        MilkQuantity quantity = MilkQuantity.fromMilliliters(request.quantityMl());
        AppUser customer = userService.getUser(request.customerId());
        requireRole(customer, UserRole.CUSTOMER);

        log.info("Attempting to book milk for customer ID: {} (Quantity: {} ml)", customer.getId(), quantity.getMilliliters());
        MilkSlot slot = milkSlotService.getSlotForUpdate(request.milkSlotId());
        if (!slot.isBookingOpenAt(LocalDateTime.now())) {
            log.warn("Booking failed: Booking window is closed for milk slot ID: {}", slot.getId());
            throw new BookingWindowClosedException("Booking window is closed for this milk slot");
        }
        ResolvedAddress resolvedAddress = resolveAddress(customer.getId(), request.addressId(), request.deliveryAddress(), request.pincode());

        if (slot.getAvailableMilkMl() >= quantity.getMilliliters()) {
            slot.reduceAvailable(quantity.getMilliliters());
            MilkBooking booking = new MilkBooking(
                    customer,
                    slot,
                    quantity.getMilliliters(),
                    resolvedAddress.deliveryAddress(),
                    resolvedAddress.pincode(),
                    BookingStatus.CONFIRMED
            );
            MilkBooking savedBooking = milkBookingRepository.save(booking);
            log.info("Milk booking confirmed successfully with ID: {} for slot ID: {}", savedBooking.getId(), slot.getId());
            notificationService.create(customer, "Milk booking confirmed", "Your milk booking is confirmed for " + quantity.getMilliliters() + " ml.");
            return MilkBookingResponse.fromBooking(savedBooking);
        }

        log.info("Milk slot ID: {} is sold out. Adding customer ID: {} to waiting list", slot.getId(), customer.getId());
        WaitingListEntry entry = new WaitingListEntry(
                customer,
                slot,
                quantity.getMilliliters(),
                resolvedAddress.deliveryAddress(),
                resolvedAddress.pincode()
        );
        WaitingListEntry savedEntry = waitingListRepository.save(entry);
        log.info("Successfully added user ID: {} to waiting list with entry ID: {}", customer.getId(), savedEntry.getId());
        notificationService.create(customer, "Added to waiting list", "Milk is currently unavailable. You have been added to the waiting list.");
        return MilkBookingResponse.fromWaitingList(savedEntry);
    }

    @Transactional
    public MilkBooking cancelBooking(Long bookingId) {
        log.info("Customer attempting to cancel milk booking ID: {}", bookingId);
        MilkBooking booking = milkBookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking cancellation failed: Booking ID {} not found", bookingId);
                    return new ResourceNotFoundException("Milk booking not found: " + bookingId);
                });
        MilkSlot slot = milkSlotService.getSlotForUpdate(booking.getMilkSlot().getId());
        if (!slot.isCancellationOpenAt(LocalDateTime.now())) {
            log.warn("Booking cancellation failed: Cancellation window is closed for milk slot ID: {}", slot.getId());
            throw new BookingWindowClosedException("Cancellation window is closed for this milk slot");
        }
        booking.cancel();
        slot.addToCancelledPool(booking.getQuantityMl());
        log.info("Milk booking ID: {} successfully cancelled. Added {} ml back to cancelled pool of slot ID: {}", bookingId, booking.getQuantityMl(), slot.getId());
        return booking;
    }

    @Transactional
    public WaitingListAllocationResponse closeCancellationAndAllocate(Long slotId) {
        log.info("Closing cancellation and starting waiting list allocation for slot ID: {}", slotId);
        MilkSlot slot = milkSlotService.getSlotForUpdate(slotId);
        slot.closeCancellation();

        List<WaitingListEntry> waitingEntries = waitingListRepository
                .findByMilkSlotIdAndStatusOrderByCreatedAtAsc(slotId, WaitingListStatus.WAITING);
        log.debug("Found {} waiting list entries for slot ID: {}", waitingEntries.size(), slotId);

        List<MilkBookingResponse> allocatedBookings = new ArrayList<>();
        int expiredCount = 0;
        boolean allocationBlocked = false;

        for (WaitingListEntry entry : waitingEntries) {
            if (!allocationBlocked && slot.getCancelledPoolMl() >= entry.getQuantityMl()) {
                slot.reduceCancelledPool(entry.getQuantityMl());
                entry.markAllocated();
                MilkBooking booking = new MilkBooking(
                        entry.getCustomer(),
                        slot,
                        entry.getQuantityMl(),
                        entry.getDeliveryAddress(),
                        entry.getPincode(),
                        BookingStatus.CONFIRMED
                );
                MilkBooking savedBooking = milkBookingRepository.save(booking);
                log.info("Allocated waiting list entry ID: {} to booking ID: {}", entry.getId(), savedBooking.getId());
                notificationService.create(entry.getCustomer(), "Waiting list allocated", "Milk became available and your waiting list request is now confirmed.");
                allocatedBookings.add(MilkBookingResponse.allocatedFromWaitingList(savedBooking));
            } else {
                allocationBlocked = true;
                entry.markExpired();
                log.info("Waiting list entry ID: {} expired due to lack of cancelled milk in slot ID: {}", entry.getId(), slotId);
                notificationService.create(entry.getCustomer(), "Waiting list expired", "Milk was not available for your waiting list request.");
                expiredCount++;
            }
        }

        log.info("Waiting list allocation complete for slot ID: {}. Allocated: {}, Expired: {}, Remaining cancelled pool: {} ml", 
                slotId, allocatedBookings.size(), expiredCount, slot.getCancelledPoolMl());
        return new WaitingListAllocationResponse(
                slot.getId(),
                allocatedBookings.size(),
                expiredCount,
                slot.getCancelledPoolMl(),
                allocatedBookings
        );
    }

    @Transactional(readOnly = true)
    public Page<MilkBooking> getSlotBookings(Long slotId, Pageable pageable) {
        log.debug("Fetching paginated bookings for slot ID: {}, pageable: {}", slotId, pageable);
        return milkBookingRepository.findByMilkSlotId(slotId, pageable);
    }

    @Transactional(readOnly = true)
    public MilkBooking getBooking(Long bookingId) {
        log.debug("Fetching milk booking ID: {}", bookingId);
        return milkBookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Milk booking not found with ID: {}", bookingId);
                    return new ResourceNotFoundException("Milk booking not found: " + bookingId);
                });
    }

    @Transactional(readOnly = true)
    public List<MilkBooking> getConfirmedBookingsByPincode(String pincode) {
        log.debug("Fetching confirmed bookings for pincode: {}", pincode);
        return milkBookingRepository.findByPincodeAndStatusOrderByIdAsc(pincode, BookingStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public Page<MilkBooking> getCustomerBookings(Long customerId, Pageable pageable) {
        log.debug("Fetching paginated bookings for customer ID: {}, pageable: {}", customerId, pageable);
        return milkBookingRepository.findByCustomerId(customerId, pageable);
    }

    @Transactional
    public MilkBooking markPaid(Long bookingId, PaymentMode paymentMode) {
        log.info("Marking milk booking ID: {} as paid via: {}", bookingId, paymentMode);
        MilkBooking booking = getBooking(bookingId);
        booking.markPaid(paymentMode);
        log.info("Milk booking ID: {} payment status updated to PAID", bookingId);
        return booking;
    }

    @Transactional(readOnly = true)
    public void requireBookingCustomer(Long bookingId, Long customerId) {
        log.debug("Validating customer ID: {} ownership of booking ID: {}", customerId, bookingId);
        MilkBooking booking = getBooking(bookingId);
        if (!booking.getCustomer().getId().equals(customerId)) {
            log.warn("Ownership validation failed. Booking ID: {} does not belong to customer ID: {}", bookingId, customerId);
            throw new AccessDeniedException("You can access only your own milk booking");
        }
    }

    private void requireRole(AppUser user, UserRole role) {
        if (user.getRole() != role) {
            log.warn("Role validation failed. User ID: {} has role: {} instead of required: {}", user.getId(), user.getRole(), role);
            throw new InvalidRoleException("User must have role " + role);
        }
    }

    private ResolvedAddress resolveAddress(Long customerId, Long addressId, String deliveryAddress, String pincode) {
        if (addressId != null) {
            Address address = addressService.getAddressForUser(customerId, addressId);
            return new ResolvedAddress(address.toSingleLine(), address.getPincode());
        }
        if (deliveryAddress == null || deliveryAddress.isBlank() || pincode == null || pincode.isBlank()) {
            Address address = addressService.getDefaultAddress(customerId);
            return new ResolvedAddress(address.toSingleLine(), address.getPincode());
        }
        return new ResolvedAddress(deliveryAddress, pincode);
    }

    private record ResolvedAddress(String deliveryAddress, String pincode) {
    }
}
