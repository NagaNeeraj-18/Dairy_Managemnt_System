package com.dairy.milk.service;

import com.dairy.milk.repository.MilkSlotRepository;
import com.dairy.milk.entity.MilkSlot;
import com.dairy.milk.dto.CreateMilkSlotRequest;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.common.exception.InvalidOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class MilkSlotService {

    private static final Logger log = LoggerFactory.getLogger(MilkSlotService.class);
    private final MilkSlotRepository milkSlotRepository;

    public MilkSlotService(MilkSlotRepository milkSlotRepository) {
        this.milkSlotRepository = milkSlotRepository;
    }

    @Transactional
    public MilkSlot createSlot(CreateMilkSlotRequest request) {
        log.info("Creating new milk slot for date: {} and slot: {} (Total milk: {} ml)", 
                request.deliveryDate(), request.deliverySlot(), request.totalMilkMl());
        if (request.totalMilkMl() % 500 != 0) {
            log.warn("Milk slot creation failed: Total milk {} ml is not in 500 ml multiples", request.totalMilkMl());
            throw new InvalidOperationException("Total milk must be entered in 500 ml multiples");
        }
        LocalDateTime bookingOpensAt = request.bookingOpensAt();
        LocalDateTime bookingClosesAt = request.bookingClosesAt();
        LocalDateTime cancellationClosesAt = request.cancellationClosesAt();

        if (bookingOpensAt == null || bookingClosesAt == null || cancellationClosesAt == null) {
            WindowDefaults defaults = defaultWindows(request.deliveryDate(), request.deliverySlot());
            bookingOpensAt = bookingOpensAt == null ? defaults.bookingOpensAt() : bookingOpensAt;
            bookingClosesAt = bookingClosesAt == null ? defaults.bookingClosesAt() : bookingClosesAt;
            cancellationClosesAt = cancellationClosesAt == null ? defaults.cancellationClosesAt() : cancellationClosesAt;
            log.debug("Using window defaults: booking opens: {}, booking closes: {}, cancellation closes: {}", 
                    bookingOpensAt, bookingClosesAt, cancellationClosesAt);
        }

        if (!bookingOpensAt.isBefore(bookingClosesAt)) {
            log.warn("Milk slot creation failed: Booking open time must be before booking close time");
            throw new InvalidOperationException("Booking open time must be before booking close time");
        }
        if (cancellationClosesAt.isBefore(bookingClosesAt)) {
            log.warn("Milk slot creation failed: Cancellation close time cannot be before booking close time");
            throw new InvalidOperationException("Cancellation close time cannot be before booking close time");
        }

        MilkSlot slot = milkSlotRepository.save(new MilkSlot(
                request.deliveryDate(),
                request.deliverySlot(),
                request.totalMilkMl(),
                bookingOpensAt,
                bookingClosesAt,
                cancellationClosesAt
        ));
        log.info("Milk slot created successfully with ID: {}", slot.getId());
        return slot;
    }

    @Transactional(readOnly = true)
    public Page<MilkSlot> getAllSlots(Pageable pageable) {
        log.debug("Fetching paginated milk slots: {}", pageable);
        return milkSlotRepository.findAll(pageable);
    }

    @Transactional
    public MilkSlot closeBooking(Long slotId) {
        log.info("Closing booking for slot ID: {}", slotId);
        MilkSlot slot = getSlotForUpdate(slotId);
        slot.closeBooking();
        log.info("Booking closed for slot ID: {}", slotId);
        return slot;
    }

    @Transactional
    public MilkSlot updateQuantity(Long slotId, int totalMilkMl) {
        log.info("Updating milk quantity for slot ID: {} to: {} ml", slotId, totalMilkMl);
        if (totalMilkMl % 500 != 0) {
            log.warn("Quantity update failed: Total milk {} ml is not in 500 ml multiples", totalMilkMl);
            throw new InvalidOperationException("Total milk must be entered in 500 ml multiples");
        }
        MilkSlot slot = getSlotForUpdate(slotId);
        try {
            slot.updateTotalMilk(totalMilkMl);
        } catch (Exception e) {
            log.warn("Quantity update failed for slot ID: {}. Error: {}", slotId, e.getMessage());
            throw e;
        }
        log.info("Milk quantity updated successfully for slot ID: {}. New total: {} ml, available: {} ml", 
                slotId, slot.getTotalMilkMl(), slot.getAvailableMilkMl());
        return slot;
    }

    @Transactional
    public MilkSlot recordMilkLoss(Long slotId, int lostAmountMl) {
        log.info("Recording milk loss for slot ID: {} of: {} ml", slotId, lostAmountMl);
        MilkSlot slot = getSlotForUpdate(slotId);
        slot.addLostMilk(lostAmountMl);
        log.info("Milk loss recorded successfully for slot ID: {}. New total loss: {} ml", slotId, slot.getLostMilkMl());
        return slot;
    }

    @Transactional(readOnly = true)
    public MilkSlot getSlot(Long slotId) {
        log.debug("Fetching milk slot ID: {}", slotId);
        return milkSlotRepository.findById(slotId)
                .orElseThrow(() -> {
                    log.warn("Milk slot not found with ID: {}", slotId);
                    return new ResourceNotFoundException("Milk slot not found: " + slotId);
                });
    }

    MilkSlot getSlotForUpdate(Long slotId) {
        log.debug("Fetching milk slot ID for update (PESSIMISTIC_WRITE lock): {}", slotId);
        return milkSlotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> {
                    log.warn("Milk slot not found with ID: {}", slotId);
                    return new ResourceNotFoundException("Milk slot not found: " + slotId);
                });
    }

    private WindowDefaults defaultWindows(java.time.LocalDate deliveryDate, com.dairy.milk.enums.DeliverySlot deliverySlot) {
        if (deliverySlot == com.dairy.milk.enums.DeliverySlot.MORNING) {
            LocalDateTime open = deliveryDate.minusDays(1).atTime(LocalTime.of(17, 0));
            LocalDateTime close = deliveryDate.minusDays(1).atTime(LocalTime.of(22, 0));
            LocalDateTime cancellationClose = deliveryDate.minusDays(1).atTime(LocalTime.of(23, 0));
            return new WindowDefaults(open, close, cancellationClose);
        }
        LocalDateTime open = deliveryDate.atTime(LocalTime.of(6, 0));
        LocalDateTime close = deliveryDate.atTime(LocalTime.of(13, 0));
        LocalDateTime cancellationClose = deliveryDate.atTime(LocalTime.of(14, 0));
        return new WindowDefaults(open, close, cancellationClose);
    }

    private record WindowDefaults(LocalDateTime bookingOpensAt, LocalDateTime bookingClosesAt, LocalDateTime cancellationClosesAt) {
    }
}
