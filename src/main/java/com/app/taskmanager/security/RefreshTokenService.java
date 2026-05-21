package com.app.taskmanager.security;

import com.app.taskmanager.dto.context.RefreshTokenContext;
import com.app.taskmanager.entity.RefreshToken;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    @Value("${refresh.token.expiration}")
    private Long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String issue(User user, Instant absoluteExpiry) {
        String raw = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .tokenHash(hash(raw))
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .absoluteExpiresAt(absoluteExpiry)
                .build();

        refreshTokenRepository.save(token);

        return raw;
    }

    @Transactional
    public RefreshTokenContext validateAndRevoke(String raw) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(raw))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token."));

        if (token.isRevoked()) {
            log.warn("Refresh token reuse detected, username={}", token.getUser().getUsername());
            refreshTokenRepository.revokeAllByActiveUser(token.getUser());
            throw new InvalidCredentialsException("Session invalidated due to token reuse. Please log in again.");
        }

        if (token.isExpired() || token.isAbsolutelyExpired()) {
            token.revoke();
            throw new InvalidCredentialsException("Refresh token has expired.");
        }

        token.revoke();
        return new RefreshTokenContext(token.getUser(), token.getAbsoluteExpiresAt());
    }

    @Transactional
    public RefreshToken revoke(String raw) { // returns token for additional information when logging
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(raw))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token."));

        token.revoke();

        return refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAllByActiveUser(User user) {
        refreshTokenRepository.revokeAllByActiveUser(user);
    }

    // - private -

    private String hash(String raw) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
