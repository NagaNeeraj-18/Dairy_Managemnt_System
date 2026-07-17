package com.dairy.delivery.repository;

import com.dairy.delivery.enums.OrderType;

import com.dairy.delivery.entity.DeliveryAssignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    boolean existsByOrderTypeAndOrderId(OrderType orderType, Long orderId);

    List<DeliveryAssignment> findByDeliveryBoyIdOrderByAssignedAtAsc(Long deliveryBoyId);

    Optional<DeliveryAssignment> findByOrderTypeAndOrderId(OrderType orderType, Long orderId);
}
