package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.LoginRequest;
import com.app.taskmanager.dto.request.RegisterRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.AuthUtils;
import com.app.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthUtils authUtils;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new InvalidCredentialsException("Username is already taken.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new InvalidCredentialsException("Email is already taken.");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @Transactional
    public void deleteOwnUser() {
        User user = authUtils.getCurrentUser();

        user.softDelete();

        userRepository.save(user);
    }
}