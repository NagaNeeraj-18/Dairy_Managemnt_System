package com.dairy.milk.entity;

import com.dairy.milk.enums.BookingStatus;
import com.dairy.common.enums.PaymentMode;
import com.dairy.common.enums.PaymentStatus;
import com.dairy.user.entity.AppUser;
import com.dairy.common.exception.InvalidOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "milk_bookings")
public class MilkBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MilkSlot milkSlot;

    @Column(nullable = false)
    private int quantityMl;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode = PaymentMode.CASH_ON_DELIVERY;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected MilkBooking() {
    }

    public MilkBooking(AppUser customer, MilkSlot milkSlot, int quantityMl, String deliveryAddress, String pincode, BookingStatus status) {
        this.customer = customer;
        this.milkSlot = milkSlot;
        this.quantityMl = quantityMl;
        this.deliveryAddress = deliveryAddress;
        this.pincode = pincode;
        this.status = status;
    }

    public void cancel() {
        if (status != BookingStatus.CONFIRMED) {
            throw new InvalidOperationException("Only confirmed bookings can be cancelled");
        }
        status = BookingStatus.CANCELLED;
    }

    public void markAssignedToDelivery() {
        if (status != BookingStatus.CONFIRMED) {
            throw new InvalidOperationException("Only confirmed bookings can be assigned");
        }
        status = BookingStatus.ASSIGNED_TO_DELIVERY;
    }

    public void markOutForDelivery() {
        if (status != BookingStatus.ASSIGNED_TO_DELIVERY) {
            throw new InvalidOperationException("Only assigned bookings can move out for delivery");
        }
        status = BookingStatus.OUT_FOR_DELIVERY;
    }

    public void markDelivered() {
        if (status != BookingStatus.ASSIGNED_TO_DELIVERY && status != BookingStatus.OUT_FOR_DELIVERY) {
            throw new InvalidOperationException("Only assigned bookings can be delivered");
        }
        status = BookingStatus.DELIVERED;
    }

    public void markPaid(PaymentMode mode) {
        paymentMode = mode;
        paymentStatus = PaymentStatus.PAID;
    }

    public Long getId() {
        return id;
    }

    public AppUser getCustomer() {
        return customer;
    }

    public MilkSlot getMilkSlot() {
        return milkSlot;
    }

    public int getQuantityMl() {
        return quantityMl;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getPincode() {
        return pincode;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
