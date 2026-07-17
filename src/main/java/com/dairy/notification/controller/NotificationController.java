package com.dairy.notification.controller;

import com.dairy.notification.dto.NotificationResponse;
import com.dairy.notification.service.NotificationService;
import com.dairy.security.service.AuthenticatedUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticatedUserService authenticatedUserService;

    public NotificationController(NotificationService notificationService, AuthenticatedUserService authenticatedUserService) {
        this.notificationService = notificationService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/users/{userId}")
    public Page<NotificationResponse> getNotifications(@PathVariable Long userId, Pageable pageable) {
        authenticatedUserService.requireCurrentUser(userId);
        return notificationService.getUserNotifications(userId, pageable)
                .map(NotificationResponse::from);
    }

    @PatchMapping("/users/{userId}/{notificationId}/read")
    public NotificationResponse markRead(@PathVariable Long userId, @PathVariable Long notificationId) {
        authenticatedUserService.requireCurrentUser(userId);
        return NotificationResponse.from(notificationService.markRead(userId, notificationId));
    }

    @PatchMapping("/users/{userId}/read-all")
    public List<NotificationResponse> markAllRead(@PathVariable Long userId) {
        authenticatedUserService.requireCurrentUser(userId);
        return notificationService.markAllRead(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
