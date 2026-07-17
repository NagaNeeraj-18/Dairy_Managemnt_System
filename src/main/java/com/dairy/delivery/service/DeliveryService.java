package com.dairy.delivery.service;

import com.dairy.delivery.repository.DeliveryAssignmentRepository;
import com.dairy.delivery.enums.OrderType;
import com.dairy.delivery.entity.DeliveryAssignment;
import com.dairy.delivery.dto.AssignByPincodeRequest;
import com.dairy.notification.service.NotificationService;
import com.dairy.milk.entity.MilkBooking;
import com.dairy.milk.service.MilkBookingService;
import com.dairy.product.entity.ProductOrder;
import com.dairy.product.service.ProductService;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.service.UserService;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.common.exception.InvalidRoleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final MilkBookingService milkBookingService;
    private final ProductService productService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final SecureRandom secureRandom = new SecureRandom();

    public DeliveryService(
            DeliveryAssignmentRepository deliveryAssignmentRepository,
            MilkBookingService milkBookingService,
            ProductService productService,
            UserService userService,
            NotificationService notificationService
    ) {
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.milkBookingService = milkBookingService;
        this.productService = productService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Transactional
    public List<DeliveryAssignment> assignMilkOrders(AssignByPincodeRequest request) {
        log.info("Assigning milk orders for pincode: {} to delivery boy ID: {}", request.pincode(), request.deliveryBoyId());
        AppUser deliveryBoy = getDeliveryBoy(request.deliveryBoyId());
        List<DeliveryAssignment> assignments = new ArrayList<>();
        List<MilkBooking> bookings = milkBookingService.getConfirmedBookingsByPincode(request.pincode());
        log.debug("Found {} confirmed milk bookings for pincode: {}", bookings.size(), request.pincode());
        for (MilkBooking booking : bookings) {
            if (!deliveryAssignmentRepository.existsByOrderTypeAndOrderId(OrderType.MILK, booking.getId())) {
                booking.markAssignedToDelivery();
                String otp = generateOtp();
                DeliveryAssignment assignment = deliveryAssignmentRepository.save(new DeliveryAssignment(
                        deliveryBoy,
                        OrderType.MILK,
                        booking.getId(),
                        booking.getDeliveryAddress(),
                        booking.getPincode(),
                        otp
                ));
                log.info("Assigned milk booking ID: {} to delivery boy ID: {}. Delivery assignment ID: {}", booking.getId(), deliveryBoy.getId(), assignment.getId());
                notificationService.create(booking.getCustomer(), "Milk out for delivery", "Your milk delivery OTP is " + otp + ".");
                assignments.add(assignment);
            }
        }
        log.info("Successfully created {} delivery assignments for milk bookings", assignments.size());
        return assignments;
    }

    @Transactional
    public List<DeliveryAssignment> assignProductOrders(AssignByPincodeRequest request) {
        log.info("Assigning product orders for pincode: {} to delivery boy ID: {}", request.pincode(), request.deliveryBoyId());
        AppUser deliveryBoy = getDeliveryBoy(request.deliveryBoyId());
        List<DeliveryAssignment> assignments = new ArrayList<>();
        List<ProductOrder> orders = productService.getPlacedOrdersByPincode(request.pincode());
        log.debug("Found {} placed product orders for pincode: {}", orders.size(), request.pincode());
        for (ProductOrder order : orders) {
            if (!deliveryAssignmentRepository.existsByOrderTypeAndOrderId(OrderType.PRODUCT, order.getId())) {
                order.markAssignedToDelivery();
                String otp = generateOtp();
                DeliveryAssignment assignment = deliveryAssignmentRepository.save(new DeliveryAssignment(
                        deliveryBoy,
                        OrderType.PRODUCT,
                        order.getId(),
                        order.getDeliveryAddress(),
                        order.getPincode(),
                        otp
                ));
                log.info("Assigned product order ID: {} to delivery boy ID: {}. Delivery assignment ID: {}", order.getId(), deliveryBoy.getId(), assignment.getId());
                notificationService.create(order.getCustomer(), "Product out for delivery", "Your product delivery OTP is " + otp + ".");
                assignments.add(assignment);
            }
        }
        log.info("Successfully created {} delivery assignments for product orders", assignments.size());
        return assignments;
    }

    @Transactional(readOnly = true)
    public List<DeliveryAssignment> getDeliveryBoyAssignments(Long deliveryBoyId) {
        log.debug("Fetching delivery assignments for delivery boy ID: {}", deliveryBoyId);
        return deliveryAssignmentRepository.findByDeliveryBoyIdOrderByAssignedAtAsc(deliveryBoyId);
    }

    @Transactional(readOnly = true)
    public DeliveryAssignment track(OrderType orderType, Long orderId) {
        log.debug("Tracking delivery assignment for order type: {} and order ID: {}", orderType, orderId);
        return deliveryAssignmentRepository.findByOrderTypeAndOrderId(orderType, orderId)
                .orElseThrow(() -> {
                    log.warn("Delivery assignment not found for order type: {} and order ID: {}", orderType, orderId);
                    return new ResourceNotFoundException("Delivery assignment not found");
                });
    }

    @Transactional(readOnly = true)
    public DeliveryAssignment trackForCustomer(OrderType orderType, Long orderId, Long customerId) {
        log.debug("Customer ID: {} tracking delivery assignment for order type: {} and order ID: {}", customerId, orderType, orderId);
        if (orderType == OrderType.MILK) {
            milkBookingService.requireBookingCustomer(orderId, customerId);
        } else {
            productService.requireOrderCustomer(orderId, customerId);
        }
        return track(orderType, orderId);
    }

    @Transactional
    public DeliveryAssignment markOutForDelivery(Long assignmentId) {
        log.info("Marking delivery assignment ID: {} as OUT_FOR_DELIVERY", assignmentId);
        DeliveryAssignment assignment = getAssignment(assignmentId);
        assignment.markOutForDelivery();
        if (assignment.getOrderType() == OrderType.MILK) {
            milkBookingService.getBooking(assignment.getOrderId()).markOutForDelivery();
        } else {
            productService.getProductOrder(assignment.getOrderId()).markOutForDelivery();
        }
        log.info("Delivery assignment ID: {} successfully moved to OUT_FOR_DELIVERY", assignmentId);
        return assignment;
    }

    @Transactional
    public DeliveryAssignment markDelivered(Long assignmentId, String otp) {
        log.info("Attempting to complete delivery for assignment ID: {} with OTP", assignmentId);
        DeliveryAssignment assignment = getAssignment(assignmentId);
        try {
            assignment.markDelivered(otp);
        } catch (Exception e) {
            log.warn("Delivery completion failed for assignment ID: {}. Invalid OTP or status check failed.", assignmentId);
            throw e;
        }

        if (assignment.getOrderType() == OrderType.MILK) {
            milkBookingService.getBooking(assignment.getOrderId()).markDelivered();
        } else {
            productService.getProductOrder(assignment.getOrderId()).markDelivered();
        }
        log.info("Delivery assignment ID: {} successfully marked as DELIVERED", assignmentId);
        return assignment;
    }

    private DeliveryAssignment getAssignment(Long assignmentId) {
        log.debug("Fetching delivery assignment ID: {}", assignmentId);
        return deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    log.warn("Delivery assignment not found with ID: {}", assignmentId);
                    return new ResourceNotFoundException("Delivery assignment not found: " + assignmentId);
                });
    }

    private AppUser getDeliveryBoy(Long deliveryBoyId) {
        log.debug("Fetching and validating delivery boy ID: {}", deliveryBoyId);
        AppUser deliveryBoy = userService.getUser(deliveryBoyId);
        if (deliveryBoy.getRole() != UserRole.DELIVERY_BOY) {
            log.warn("User ID {} is not a delivery boy", deliveryBoyId);
            throw new InvalidRoleException("User must have role DELIVERY_BOY");
        }
        return deliveryBoy;
    }

    private String generateOtp() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }
}
