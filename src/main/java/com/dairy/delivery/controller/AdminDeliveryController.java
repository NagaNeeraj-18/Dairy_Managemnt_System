package com.dairy.delivery.controller;

import com.dairy.delivery.service.DeliveryService;

import com.dairy.delivery.dto.AssignByPincodeRequest;
import com.dairy.delivery.dto.DeliveryAssignmentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/delivery")
public class AdminDeliveryController {

    private final DeliveryService deliveryService;

    public AdminDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/assign-milk-by-pincode")
    public List<DeliveryAssignmentResponse> assignMilkOrders(@Valid @RequestBody AssignByPincodeRequest request) {
        return deliveryService.assignMilkOrders(request).stream().map(DeliveryAssignmentResponse::from).toList();
    }

    @PostMapping("/assign-products-by-pincode")
    public List<DeliveryAssignmentResponse> assignProductOrders(@Valid @RequestBody AssignByPincodeRequest request) {
        return deliveryService.assignProductOrders(request).stream().map(DeliveryAssignmentResponse::from).toList();
    }
}
