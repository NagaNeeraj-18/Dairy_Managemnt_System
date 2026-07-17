package com.dairy.delivery.controller;

import com.dairy.delivery.service.DeliveryService;
import com.dairy.security.service.AuthenticatedUserService;

import com.dairy.delivery.enums.OrderType;

import com.dairy.delivery.dto.DeliveryAssignmentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/deliveries")
public class CustomerTrackingController {

    private final DeliveryService deliveryService;
    private final AuthenticatedUserService authenticatedUserService;

    public CustomerTrackingController(DeliveryService deliveryService, AuthenticatedUserService authenticatedUserService) {
        this.deliveryService = deliveryService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/{orderType}/{orderId}")
    public DeliveryAssignmentResponse track(@PathVariable OrderType orderType, @PathVariable Long orderId) {
        return DeliveryAssignmentResponse.from(deliveryService.trackForCustomer(orderType, orderId, authenticatedUserService.currentUserId()));
    }
}
