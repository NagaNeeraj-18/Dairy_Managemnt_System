package com.dairy.feedback.service;

import com.dairy.delivery.enums.OrderType;
import com.dairy.feedback.repository.FeedbackRepository;
import com.dairy.feedback.entity.Feedback;
import com.dairy.feedback.dto.CreateFeedbackRequest;
import com.dairy.milk.enums.BookingStatus;
import com.dairy.milk.service.MilkBookingService;
import com.dairy.product.enums.ProductOrderStatus;
import com.dairy.product.service.ProductService;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.service.UserService;
import com.dairy.common.exception.InvalidRoleException;
import com.dairy.common.exception.InvalidOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;
    private final MilkBookingService milkBookingService;
    private final ProductService productService;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            UserService userService,
            MilkBookingService milkBookingService,
            ProductService productService
    ) {
        this.feedbackRepository = feedbackRepository;
        this.userService = userService;
        this.milkBookingService = milkBookingService;
        this.productService = productService;
    }

    @Transactional
    public Feedback createFeedback(CreateFeedbackRequest request) {
        log.info("Attempting to submit feedback for customer ID: {} on order ID: {} ({})", 
                request.customerId(), request.orderId(), request.orderType());
        AppUser customer = userService.getUser(request.customerId());
        if (customer.getRole() != UserRole.CUSTOMER) {
            log.warn("Feedback submission failed: User ID {} does not have CUSTOMER role", request.customerId());
            throw new InvalidRoleException("User must have role CUSTOMER");
        }
        try {
            validateFeedbackAllowed(request, customer.getId());
        } catch (Exception e) {
            log.warn("Feedback submission failed validation: {}", e.getMessage());
            throw e;
        }
        Feedback feedback = feedbackRepository.save(new Feedback(
                customer,
                request.orderType(),
                request.orderId(),
                request.productRating(),
                request.deliveryBoyRating(),
                request.comment()
        ));
        log.info("Feedback submitted successfully with ID: {}", feedback.getId());
        return feedback;
    }

    @Transactional(readOnly = true)
    public Page<Feedback> getAllFeedback(Pageable pageable) {
        log.debug("Fetching paginated feedback: {}", pageable);
        return feedbackRepository.findAll(pageable);
    }

    private void validateFeedbackAllowed(CreateFeedbackRequest request, Long customerId) {
        if (feedbackRepository.existsByOrderTypeAndOrderIdAndCustomerId(request.orderType(), request.orderId(), customerId)) {
            throw new InvalidOperationException("Feedback already submitted for this order");
        }

        if (request.orderType() == OrderType.MILK) {
            milkBookingService.requireBookingCustomer(request.orderId(), customerId);
            if (milkBookingService.getBooking(request.orderId()).getStatus() != BookingStatus.DELIVERED) {
                throw new InvalidOperationException("Feedback can be submitted only after milk delivery");
            }
            return;
        }

        productService.requireOrderCustomer(request.orderId(), customerId);
        if (productService.getProductOrder(request.orderId()).getStatus() != ProductOrderStatus.DELIVERED) {
            throw new InvalidOperationException("Feedback can be submitted only after product delivery");
        }
    }
}
