package com.dairy.user.service;

import com.dairy.user.repository.UserRepository;
import com.dairy.user.entity.AppUser;
import com.dairy.user.dto.CreateUserRequest;
import com.dairy.common.exception.DuplicateResourceException;
import com.dairy.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser createUser(CreateUserRequest request) {
        log.info("Attempting to create user with email: {} and role: {}", request.email(), request.role());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("User creation failed: Email {} already exists", request.email());
            throw new DuplicateResourceException("Email is already registered");
        }
        if (userRepository.existsByPhone(request.phone())) {
            log.warn("User creation failed: Phone {} already exists", request.phone());
            throw new DuplicateResourceException("Phone is already registered");
        }
        AppUser savedUser = userRepository.save(new AppUser(
                request.name(),
                request.email(),
                request.phone(),
                passwordEncoder.encode(request.password()),
                request.role()
        ));
        log.info("User created successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public AppUser getUser(Long userId) {
        log.debug("Fetching user details for ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });
    }

    @Transactional(readOnly = true)
    public AppUser getUserByEmail(String email) {
        log.debug("Fetching user details for email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found: " + email);
                });
    }
}
