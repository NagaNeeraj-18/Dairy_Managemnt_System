package com.dairy.milk.entity;

import com.dairy.milk.enums.WaitingListStatus;

import com.dairy.user.entity.AppUser;
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
@Table(name = "waiting_list_entries")
public class WaitingListEntry {

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
    private WaitingListStatus status = WaitingListStatus.WAITING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected WaitingListEntry() {
    }

    public WaitingListEntry(AppUser customer, MilkSlot milkSlot, int quantityMl, String deliveryAddress, String pincode) {
        this.customer = customer;
        this.milkSlot = milkSlot;
        this.quantityMl = quantityMl;
        this.deliveryAddress = deliveryAddress;
        this.pincode = pincode;
    }

    public void markAllocated() {
        status = WaitingListStatus.ALLOCATED;
    }

    public void markExpired() {
        status = WaitingListStatus.EXPIRED;
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

    public WaitingListStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
