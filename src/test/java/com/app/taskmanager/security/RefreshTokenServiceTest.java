package com.app.taskmanager.security;

import com.app.taskmanager.dto.context.RefreshTokenContext;
import com.app.taskmanager.entity.RefreshToken;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private static final Long refreshExpiration = 604800000L;

    private static final Long refreshAbsoluteExpiry = 2592000000L;

    private User user;

    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("John")
                .password("JohnPass1")
                .email("John@email.com")
                .role(Role.USER)
                .build();

        refreshToken = RefreshToken.builder()
                .tokenHash("tokenHash")
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .absoluteExpiresAt(Instant.now().plusMillis(refreshAbsoluteExpiry))
                .build();
    }

    // - Issue -

    @Test
    void issue_returnsRawAndSavesToken() {
        setField(refreshTokenService, "refreshExpiration", refreshExpiration);
        String raw = refreshTokenService.issue(user, Instant.now().plusMillis(refreshAbsoluteExpiry));

        assertThat(raw).isNotNull();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    // - ValidateAndRevoke -

    @Test
    void validateAndRevoke_SuccessfullyValidatesAndRevokesToken() {
        when(refreshTokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.of(refreshToken));

        RefreshTokenContext context = refreshTokenService.validateAndRevoke("raw");

        assertThat(refreshToken.isRevoked()).isTrue();
        assertThat(context.user()).isEqualTo(refreshToken.getUser());
        assertThat(context.absoluteExpiresAt()).isEqualTo(refreshToken.getAbsoluteExpiresAt());
    }

    @Test
    void validateAndRevoke_ThrowsException_ForInvalidRefreshToken() {
        when(refreshTokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateAndRevoke("raw"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid refresh token.");
    }

    @Test
    void validateAndRevoke_ThrowsException_ForRevokedToken() {
        when(refreshTokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.of(refreshToken));
        setField(refreshToken, "revoked", true);

        assertThatThrownBy(() -> refreshTokenService.validateAndRevoke("raw"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Session invalidated due to token reuse. Please log in again.");

        verify(refreshTokenRepository).revokeAllByActiveUser(refreshToken.getUser());
    }

    @Test
    void validateAndRevoke_TokenIsExpiredAbsolutelyOrNot() {
        when(refreshTokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.of(refreshToken));
        setField(refreshToken, "expiresAt", Instant.now().minusMillis(999999L));
        setField(refreshToken, "absoluteExpiresAt", Instant.now().minusMillis(999999L));

        assertThatThrownBy(() -> refreshTokenService.validateAndRevoke("raw"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Refresh token has expired.");

        assertThat(refreshToken.isRevoked()).isTrue();
    }

    // - Revoke -

    @Test
    void revoke_TokenIsSuccessfullyRevoked() {
        when(refreshTokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.of(refreshToken));

        refreshTokenService.revoke("raw");

        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void revoke_ThrowsException_ForInvalidRefreshToken() {
        when(refreshTokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.revoke("raw"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid refresh token.");
    }
}
