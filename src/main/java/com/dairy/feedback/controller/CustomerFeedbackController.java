package com.dairy.feedback.controller;

import com.dairy.feedback.service.FeedbackService;
import com.dairy.security.service.AuthenticatedUserService;

import com.dairy.feedback.dto.CreateFeedbackRequest;
import com.dairy.feedback.dto.FeedbackResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/feedback")
public class CustomerFeedbackController {

    private final FeedbackService feedbackService;
    private final AuthenticatedUserService authenticatedUserService;

    public CustomerFeedbackController(FeedbackService feedbackService, AuthenticatedUserService authenticatedUserService) {
        this.feedbackService = feedbackService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        authenticatedUserService.requireCurrentUser(request.customerId());
        return FeedbackResponse.from(feedbackService.createFeedback(request));
    }
}
