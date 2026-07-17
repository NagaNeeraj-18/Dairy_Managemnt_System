package com.dairy.delivery.entity;

import com.dairy.delivery.enums.OrderType;

import com.dairy.delivery.enums.DeliveryStatus;

import com.dairy.user.entity.AppUser;
import com.dairy.common.exception.InvalidOperationException;
import com.dairy.common.exception.OtpVerificationException;
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
@Table(name = "delivery_assignments")
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser deliveryBoy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String otp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.ASSIGNED;

    @Column(nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    private Instant deliveredAt;

    protected DeliveryAssignment() {
    }

    public DeliveryAssignment(AppUser deliveryBoy, OrderType orderType, Long orderId, String deliveryAddress, String pincode, String otp) {
        this.deliveryBoy = deliveryBoy;
        this.orderType = orderType;
        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
        this.pincode = pincode;
        this.otp = otp;
    }

    public void markOutForDelivery() {
        if (status != DeliveryStatus.ASSIGNED) {
            throw new InvalidOperationException("Only assigned deliveries can move out for delivery");
        }
        status = DeliveryStatus.OUT_FOR_DELIVERY;
    }

    public void markDelivered(String providedOtp) {
        if (!otp.equals(providedOtp)) {
            throw new OtpVerificationException("Invalid delivery OTP");
        }
        if (status != DeliveryStatus.ASSIGNED && status != DeliveryStatus.OUT_FOR_DELIVERY) {
            throw new InvalidOperationException("Delivery cannot be completed from current status");
        }
        status = DeliveryStatus.DELIVERED;
        deliveredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getDeliveryBoy() {
        return deliveryBoy;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getPincode() {
        return pincode;
    }

    public String getOtp() {
        return otp;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }
}
