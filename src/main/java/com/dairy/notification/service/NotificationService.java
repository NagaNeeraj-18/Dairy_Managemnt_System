package com.dairy.notification.service;

import com.dairy.notification.entity.Notification;
import com.dairy.notification.repository.NotificationRepository;
import com.dairy.user.entity.AppUser;
import com.dairy.user.service.UserService;
import com.dairy.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Transactional
    public Notification create(AppUser user, String title, String message) {
        log.info("Creating notification for user ID: {} - Title: '{}'", user.getId(), title);
        Notification savedNotification = notificationRepository.save(new Notification(user, title, message));
        log.debug("Notification created with ID: {}", savedNotification.getId());
        return savedNotification;
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching paginated notifications for user ID: {}, pageable: {}", userId, pageable);
        userService.getUser(userId);
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public Notification markRead(Long userId, Long notificationId) {
        log.info("Marking notification ID: {} as read for user ID: {}", notificationId, userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("Notification read update failed: Notification ID {} not found", notificationId);
                    return new ResourceNotFoundException("Notification not found: " + notificationId);
                });
        if (!notification.getUser().getId().equals(userId)) {
            log.warn("Notification ownership check failed. Notification ID: {} does not belong to user ID: {}", notificationId, userId);
            throw new AccessDeniedException("You can update only your own notifications");
        }
        notification.markRead();
        log.info("Notification ID: {} marked as read", notificationId);
        return notification;
    }

    @Transactional
    public List<Notification> markAllRead(Long userId) {
        log.info("Marking all notifications as read for user ID: {}", userId);
        userService.getUser(userId);
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        notifications.forEach(Notification::markRead);
        log.info("Marked {} notifications as read for user ID: {}", notifications.size(), userId);
        return notifications;
    }
}
