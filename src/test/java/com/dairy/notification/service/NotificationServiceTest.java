package com.dairy.notification.service;

import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.notification.entity.Notification;
import com.dairy.notification.enums.NotificationStatus;
import com.dairy.notification.repository.NotificationRepository;
import com.dairy.user.entity.AppUser;
import com.dairy.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    private AppUser appUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        appUser = mock(AppUser.class);
        when(appUser.getId()).thenReturn(1L);
        notification = new Notification(appUser, "Test Title", "Test Message");
    }

    @Test
    void create_Success() {
        when(notificationRepository.save(any())).thenReturn(notification);

        Notification result = notificationService.create(appUser, "Test Title", "Test Message");

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    void getUserNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserId(1L, pageable)).thenReturn(page);

        Page<Notification> result = notificationService.getUserNotifications(1L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(userService, times(1)).getUser(1L);
    }

    @Test
    void markRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Notification result = notificationService.markRead(1L, 1L);

        assertNotNull(result);
        assertEquals(NotificationStatus.READ, result.getStatus());
    }

    @Test
    void markRead_Unauthorized() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () -> notificationService.markRead(2L, 1L));
    }

    @Test
    void markRead_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markRead(1L, 1L));
    }

    @Test
    void markAllRead_Success() {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(notification);
        when(notificationRepository.findByUserId(1L)).thenReturn(notifications);

        List<Notification> result = notificationService.markAllRead(1L);

        assertEquals(1, result.size());
        assertEquals(NotificationStatus.READ, result.get(0).getStatus());
        verify(userService, times(1)).getUser(1L);
    }
}
