package com.dairy.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "offline_sales")
public class OfflineSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String unitLabel;

    @Column(nullable = false)
    private BigDecimal amount;

    private String customerName;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected OfflineSale() {
    }

    public OfflineSale(String itemName, int quantity, String unitLabel, BigDecimal amount, String customerName) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitLabel = unitLabel;
        this.amount = amount;
        this.customerName = customerName;
    }

    public Long getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCustomerName() {
        return customerName;
    }
}
