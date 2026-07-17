package com.dairy.security.dto;

import com.dairy.user.enums.UserRole;

public record AuthResponse(
        String token,
        String refreshToken,
        Long userId,
        String name,
        String email,
        UserRole role
) {
}
