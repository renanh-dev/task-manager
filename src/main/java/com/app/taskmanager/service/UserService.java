package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.UserUpdateRequest;
import com.app.taskmanager.dto.response.UserResponse;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.exception.ResourceNotFoundException;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthUtils authUtils;

    @Transactional
    public void deleteOwnUser() {
        Long userId = authUtils.getCurrentUserId();

        userRepository.softDeleteByUserId(userId);

        log.info("User soft deleted, userId={}", userId);
    }

    @Transactional
    public UserResponse changeCredentials(UserUpdateRequest request) {
        User user = userRepository.findById(authUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.username() != null && userRepository.existsByUsername(request.username()) && !request.username().equals(user.getUsername())) {
            throw new InvalidCredentialsException("Username is already taken.");
        }

        if (request.email() != null && userRepository.existsByEmail(request.email()) && !request.email().equals(user.getEmail())) {
            throw new InvalidCredentialsException("Email is already taken.");
        }

        if (request.username() != null) user.changeUsername(request.username());
        if (request.password() != null) user.changePassword(passwordEncoder.encode(request.password()));
        if (request.email() != null)    user.changeEmail(request.email());

        log.info("Credentials updated, userId={}", user.getId());

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse getCurrentUser() {
        return UserResponse.from(userRepository.findById(authUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    public User getReferenceById(Long userId) { // when in need of lazy initialization/proxy
        return userRepository.getReferenceById(userId);
    }
}