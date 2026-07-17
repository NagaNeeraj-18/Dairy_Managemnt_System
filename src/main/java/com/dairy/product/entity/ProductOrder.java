package com.dairy.product.entity;

import com.dairy.product.enums.ProductOrderStatus;
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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_orders")
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductOrderStatus status = ProductOrderStatus.PLACED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode = PaymentMode.CASH_ON_DELIVERY;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ProductOrder() {
    }

    public ProductOrder(AppUser customer, Product product, int quantity, String deliveryAddress, String pincode) {
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
        this.deliveryAddress = deliveryAddress;
        this.pincode = pincode;
        this.totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void markAssignedToDelivery() {
        if (status != ProductOrderStatus.PLACED) {
            throw new InvalidOperationException("Only placed product orders can be assigned");
        }
        status = ProductOrderStatus.ASSIGNED_TO_DELIVERY;
    }

    public void markOutForDelivery() {
        if (status != ProductOrderStatus.ASSIGNED_TO_DELIVERY) {
            throw new InvalidOperationException("Only assigned product orders can move out for delivery");
        }
        status = ProductOrderStatus.OUT_FOR_DELIVERY;
    }

    public void markDelivered() {
        if (status != ProductOrderStatus.ASSIGNED_TO_DELIVERY && status != ProductOrderStatus.OUT_FOR_DELIVERY) {
            throw new InvalidOperationException("Only assigned product orders can be delivered");
        }
        status = ProductOrderStatus.DELIVERED;
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

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getPincode() {
        return pincode;
    }

    public ProductOrderStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }
}
