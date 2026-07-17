package com.dairy.security.controller;

import com.dairy.security.service.JwtService;
import com.dairy.security.service.RefreshTokenService;
import com.dairy.security.service.AuthenticatedUserService;
import com.dairy.security.dto.AuthResponse;
import com.dairy.security.dto.LoginRequest;
import com.dairy.security.dto.TokenRefreshRequest;
import com.dairy.security.entity.RefreshToken;
import com.dairy.user.entity.AppUser;
import com.dairy.user.service.UserService;
import com.dairy.user.dto.CreateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticatedUserService authenticatedUserService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody CreateUserRequest request) {
        AppUser user = userService.createUser(request);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return toAuthResponse(user, refreshToken.getToken());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        AppUser user = userService.getUserByEmail(request.email());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return toAuthResponse(user, refreshToken.getToken());
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
                    return toAuthResponse(user, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new com.dairy.common.exception.ResourceNotFoundException("Refresh token is not in database"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Long currentUserId = authenticatedUserService.currentUserId();
        refreshTokenService.deleteByUserId(currentUserId);
        return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
    }

    private AuthResponse toAuthResponse(AppUser user, String refreshToken) {
        return new AuthResponse(
                jwtService.createToken(user),
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
