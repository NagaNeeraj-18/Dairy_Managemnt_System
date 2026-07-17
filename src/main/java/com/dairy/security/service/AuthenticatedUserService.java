package com.dairy.security.service;

import com.dairy.user.entity.AppUser;
import com.dairy.user.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    private final UserService userService;

    public AuthenticatedUserService(UserService userService) {
        this.userService = userService;
    }

    public AppUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user not found");
        }
        return userService.getUserByEmail(authentication.getName());
    }

    public Long currentUserId() {
        return currentUser().getId();
    }

    public void requireCurrentUser(Long userId) {
        if (!currentUserId().equals(userId)) {
            throw new AccessDeniedException("You can access only your own customer data");
        }
    }
}
