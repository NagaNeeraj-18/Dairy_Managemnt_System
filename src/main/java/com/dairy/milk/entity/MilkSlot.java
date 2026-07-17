package com.dairy.milk.entity;

import com.dairy.milk.enums.WindowStatus;
import com.dairy.milk.enums.DeliverySlot;
import com.dairy.common.exception.InsufficientStockException;
import com.dairy.common.exception.InvalidOperationException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "milk_slots",
        uniqueConstraints = @UniqueConstraint(name = "uk_milk_slot_date_delivery", columnNames = {"delivery_date", "delivery_slot"})
)
public class MilkSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_slot", nullable = false)
    private DeliverySlot deliverySlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WindowStatus bookingStatus = WindowStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WindowStatus cancellationStatus = WindowStatus.OPEN;

    @Column(nullable = false)
    private int totalMilkMl;

    @Column(nullable = false)
    private int availableMilkMl;

    @Column(nullable = false)
    private int cancelledPoolMl;

    @Column(nullable = false)
    private int lostMilkMl = 0;

    @Column(nullable = false)
    private LocalDateTime bookingOpensAt;

    @Column(nullable = false)
    private LocalDateTime bookingClosesAt;

    @Column(nullable = false)
    private LocalDateTime cancellationClosesAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected MilkSlot() {
    }

    public MilkSlot(LocalDate deliveryDate, DeliverySlot deliverySlot, int totalMilkMl, LocalDateTime bookingOpensAt, LocalDateTime bookingClosesAt, LocalDateTime cancellationClosesAt) {
        this.deliveryDate = deliveryDate;
        this.deliverySlot = deliverySlot;
        this.totalMilkMl = totalMilkMl;
        this.availableMilkMl = totalMilkMl;
        this.bookingOpensAt = bookingOpensAt;
        this.bookingClosesAt = bookingClosesAt;
        this.cancellationClosesAt = cancellationClosesAt;
    }

    public void reduceAvailable(int quantityMl) {
        if (availableMilkMl < quantityMl) {
            throw new InsufficientStockException("Not enough milk available");
        }
        availableMilkMl -= quantityMl;
    }

    public void updateTotalMilk(int newTotalMilkMl) {
        if (newTotalMilkMl < 0) {
            throw new InvalidOperationException("Total milk cannot be negative");
        }
        int bookedMilkMl = totalMilkMl - availableMilkMl;
        if (newTotalMilkMl < bookedMilkMl) {
            throw new InvalidOperationException("Total milk cannot be less than already booked milk");
        }
        totalMilkMl = newTotalMilkMl;
        availableMilkMl = newTotalMilkMl - bookedMilkMl;
    }

    public void addToCancelledPool(int quantityMl) {
        cancelledPoolMl += quantityMl;
    }

    public void reduceCancelledPool(int quantityMl) {
        if (cancelledPoolMl < quantityMl) {
            throw new InsufficientStockException("Not enough cancelled milk available");
        }
        cancelledPoolMl -= quantityMl;
    }

    public void closeBooking() {
        bookingStatus = WindowStatus.CLOSED;
    }

    public void closeCancellation() {
        cancellationStatus = WindowStatus.CLOSED;
    }

    public boolean isBookingOpenAt(LocalDateTime time) {
        return bookingStatus == WindowStatus.OPEN
                && !time.isBefore(bookingOpensAt)
                && time.isBefore(bookingClosesAt);
    }

    public boolean isCancellationOpenAt(LocalDateTime time) {
        return cancellationStatus == WindowStatus.OPEN && time.isBefore(cancellationClosesAt);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public DeliverySlot getDeliverySlot() {
        return deliverySlot;
    }

    public WindowStatus getBookingStatus() {
        return bookingStatus;
    }

    public WindowStatus getCancellationStatus() {
        return cancellationStatus;
    }

    public int getTotalMilkMl() {
        return totalMilkMl;
    }

    public int getAvailableMilkMl() {
        return availableMilkMl;
    }

    public int getCancelledPoolMl() {
        return cancelledPoolMl;
    }

    public LocalDateTime getBookingOpensAt() {
        return bookingOpensAt;
    }

    public LocalDateTime getBookingClosesAt() {
        return bookingClosesAt;
    }

    public LocalDateTime getCancellationClosesAt() {
        return cancellationClosesAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getLostMilkMl() {
        return lostMilkMl;
    }

    public void setLostMilkMl(int lostMilkMl) {
        this.lostMilkMl = lostMilkMl;
    }

    public void addLostMilk(int quantityMl) {
        if (quantityMl < 0) {
            throw new InvalidOperationException("Lost milk quantity cannot be negative");
        }
        this.lostMilkMl += quantityMl;
    }
}
