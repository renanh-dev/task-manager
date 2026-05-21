package com.app.taskmanager.service;

import com.app.taskmanager.dto.context.RefreshTokenContext;
import com.app.taskmanager.dto.request.LoginRequest;
import com.app.taskmanager.dto.request.RefreshTokenRequest;
import com.app.taskmanager.dto.request.RegisterRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import com.app.taskmanager.entity.RefreshToken;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.metrics.AppMetrics;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.JwtService;
import com.app.taskmanager.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.app.taskmanager.util.TransactionUtils.afterCommit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AppMetrics appMetrics;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    @Value("${refresh.absolute.expiry}")
    private Long absoluteExpiresAt;

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

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.issue(user, Instant.now().plusMillis(absoluteExpiresAt));

        afterCommit(appMetrics::incrementRegistrations);

        log.info("User registered: {}", user.getUsername());
        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsernameOrEmail(request.identifier(), request.identifier())
                .orElseThrow(() -> {
                    log.warn("Login failed, reason=user_not_found, identifier={}", request.identifier());
                    return new InvalidCredentialsException("Invalid credentials.");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed, reason=invalid_password, identifier={}", request.identifier());
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.issue(user, Instant.now().plusMillis(absoluteExpiresAt));

        log.info("User logged in, username={}", user.getUsername());
        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenContext context = refreshTokenService.validateAndRevoke(request.refreshToken());
        String newAccessToken = jwtService.generateToken(context.user());
        String newRefreshToken = refreshTokenService.issue(context.user(), context.absoluteExpiresAt());

        log.info("Tokens rotated, username={}", context.user());

        return new AuthResponse(newAccessToken, newRefreshToken, context.user().getUsername(), context.user().getRole());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenService.revoke(request.refreshToken());
        log.info("User logged out, username={}, tokenId={}", token.getUser().getUsername(), token.getId());
    }
}
