package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.LoginRequest;
import com.app.taskmanager.dto.request.RegisterRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.metrics.AppMetrics;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.JwtProcessing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.app.taskmanager.util.TransactionUtils.afterCommit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AppMetrics appMetrics;

    private final JwtProcessing jwtProcessing;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed, reason=username_taken, username={}", request.username());
            throw new InvalidCredentialsException("Username is already taken.");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed, reason=email_taken, email={}", request.email());
            throw new InvalidCredentialsException("Email is already taken.");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtProcessing.generateToken(user);

        afterCommit(appMetrics::incrementRegistrations);

        log.info("User registered: {}", user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Login failed, reason=user_not_found, username={}", request.username());
                    return new InvalidCredentialsException("Invalid credentials.");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed, reason=invalid_password, username={}", request.username());
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        String token = jwtProcessing.generateToken(user);

        log.info("User logged in, username={}", user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
