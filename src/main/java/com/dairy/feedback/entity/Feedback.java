package com.dairy.feedback.entity;

import com.dairy.delivery.enums.OrderType;
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
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private int productRating;

    @Column(nullable = false)
    private int deliveryBoyRating;

    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Feedback() {
    }

    public Feedback(AppUser customer, OrderType orderType, Long orderId, int productRating, int deliveryBoyRating, String comment) {
        this.customer = customer;
        this.orderType = orderType;
        this.orderId = orderId;
        this.productRating = productRating;
        this.deliveryBoyRating = deliveryBoyRating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public AppUser getCustomer() {
        return customer;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public int getProductRating() {
        return productRating;
    }

    public int getDeliveryBoyRating() {
        return deliveryBoyRating;
    }

    public String getComment() {
        return comment;
    }
}
