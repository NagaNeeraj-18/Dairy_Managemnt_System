package com.dairy.feedback.repository;

import com.dairy.feedback.entity.Feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByOrderTypeAndOrderIdAndCustomerId(com.dairy.delivery.enums.OrderType orderType, Long orderId, Long customerId);

    @Query("select coalesce(avg(f.productRating), 0) from Feedback f")
    double averageProductRating();

    @Query("select coalesce(avg(f.deliveryBoyRating), 0) from Feedback f")
    double averageDeliveryBoyRating();
}
