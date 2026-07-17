package com.dairy.user.dto;

import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        UserRole role
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }
}
