package com.dairy.user.service;

import com.dairy.common.exception.DuplicateResourceException;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.user.dto.CreateUserRequest;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private AppUser appUser;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest("John Doe", "john@example.com", "1234567890", "password123", UserRole.CUSTOMER);
        appUser = new AppUser("John Doe", "john@example.com", "1234567890", "encodedPassword", UserRole.CUSTOMER);
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(appUser);

        AppUser result = userService.createUser(createUserRequest);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_DuplicateEmail() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_DuplicatePhone() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhone(any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));

        AppUser result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(1L));
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(appUser));

        AppUser result = userService.getUserByEmail("john@example.com");

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("john@example.com"));
    }
}
