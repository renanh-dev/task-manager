package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.UserUpdateRequest;
import com.app.taskmanager.dto.response.UserResponse;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.exception.InvalidCredentialsException;
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
        User user = authUtils.getCurrentUser();
        user.softDelete();

        userRepository.save(user);
        log.info("User soft deleted, userId={}", user.getId());
    }

    @Transactional
    public UserResponse changeCredentials(UserUpdateRequest request) {
        User user = authUtils.getCurrentUser();

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
        return UserResponse.from(authUtils.getCurrentUser());
    }
}