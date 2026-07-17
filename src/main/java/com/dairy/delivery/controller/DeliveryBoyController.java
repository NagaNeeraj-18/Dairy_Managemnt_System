package com.dairy.delivery.controller;

import com.dairy.delivery.service.DeliveryService;

import com.dairy.delivery.dto.DeliveryAssignmentResponse;
import com.dairy.delivery.dto.VerifyOtpRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryBoyController {

    private final DeliveryService deliveryService;

    public DeliveryBoyController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/boys/{deliveryBoyId}/assignments")
    public List<DeliveryAssignmentResponse> getAssignments(@PathVariable Long deliveryBoyId) {
        return deliveryService.getDeliveryBoyAssignments(deliveryBoyId).stream()
                .map(DeliveryAssignmentResponse::from)
                .toList();
    }

    @PostMapping("/assignments/{assignmentId}/out-for-delivery")
    public DeliveryAssignmentResponse markOutForDelivery(@PathVariable Long assignmentId) {
        return DeliveryAssignmentResponse.from(deliveryService.markOutForDelivery(assignmentId));
    }

    @PostMapping("/assignments/{assignmentId}/deliver")
    public DeliveryAssignmentResponse markDelivered(@PathVariable Long assignmentId, @Valid @RequestBody VerifyOtpRequest request) {
        return DeliveryAssignmentResponse.from(deliveryService.markDelivered(assignmentId, request.otp()));
    }
}
