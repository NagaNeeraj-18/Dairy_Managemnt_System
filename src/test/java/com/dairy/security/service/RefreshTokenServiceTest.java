package com.dairy.security.service;

import com.dairy.common.exception.InvalidOperationException;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.security.entity.RefreshToken;
import com.dairy.security.repository.RefreshTokenRepository;
import com.dairy.user.entity.AppUser;
import com.dairy.user.repository.UserRepository;
import com.dairy.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    private RefreshTokenService refreshTokenService;
    private AppUser appUser;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, 7);
        appUser = new AppUser("John Doe", "john@example.com", "1234567890", "encodedPassword", UserRole.CUSTOMER);
        refreshToken = new RefreshToken(appUser, "test-token-uuid", Instant.now().plusSeconds(3600));
    }

    @Test
    void findByToken_Success() {
        when(refreshTokenRepository.findByToken("test-token-uuid")).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken("test-token-uuid");

        assertTrue(result.isPresent());
        assertEquals("test-token-uuid", result.get().getToken());
    }

    @Test
    void createRefreshToken_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertNotNull(result);
        assertEquals(appUser, result.getUser());
        assertNotNull(result.getToken());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
        verify(refreshTokenRepository, times(1)).deleteByUser(appUser);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> refreshTokenService.createRefreshToken(1L));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void verifyExpiration_NotExpired() {
        RefreshToken result = refreshTokenService.verifyExpiration(refreshToken);
        assertEquals(refreshToken, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_Expired() {
        RefreshToken expiredToken = new RefreshToken(appUser, "expired-token", Instant.now().minusSeconds(10));

        assertThrows(InvalidOperationException.class, () -> refreshTokenService.verifyExpiration(expiredToken));
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    void deleteByUserId_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(refreshTokenRepository.deleteByUser(appUser)).thenReturn(1);

        int deletedCount = refreshTokenService.deleteByUserId(1L);

        assertEquals(1, deletedCount);
        verify(refreshTokenRepository, times(1)).deleteByUser(appUser);
    }
}
