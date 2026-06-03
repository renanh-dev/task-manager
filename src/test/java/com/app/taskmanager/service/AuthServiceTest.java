package com.app.taskmanager.service;

import com.app.taskmanager.dto.context.RefreshTokenContext;
import com.app.taskmanager.dto.request.LoginRequest;
import com.app.taskmanager.dto.request.RefreshTokenRequest;
import com.app.taskmanager.dto.request.RegisterRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.exception.ResourceConflictException;
import com.app.taskmanager.metrics.AppMetrics;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.JwtService;
import com.app.taskmanager.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AppMetrics appMetrics;

    private RegisterRequest regRequest;
    private LoginRequest logRequest;
    private RefreshTokenRequest refreshRequest;
    private RefreshTokenContext refreshContext;

    private final String username = "John";
    private final String password = "JohnPass";
    private final String email = "John@johnmail.com";
    private final String oldRefreshToken = "oldRefreshToken";

    private User user;

    private static final Long refreshAbsoluteExpiry = 2592000000L;

    @BeforeEach
    void setUp() {
        setField(authService, "refreshAbsoluteExpiry", refreshAbsoluteExpiry);

        user = buildUser(1L, email, username, password);

        regRequest = new RegisterRequest(username, password, email);
        logRequest = new LoginRequest(username, password);
        refreshRequest = new RefreshTokenRequest(oldRefreshToken);
        refreshContext = new RefreshTokenContext(user, Instant.now().plusMillis(refreshAbsoluteExpiry));
    }

    // - Register -

    @Test
    void register_UserIsCreatedAndSaved() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
        when(refreshTokenService.issue(any(User.class), any(Instant.class))).thenReturn("refreshToken");

        AuthResponse response = authService.register(regRequest);

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        verify(refreshTokenService).issue(any(User.class),any(Instant.class));
        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
    }

    @Test
    void register_UserAlreadyTaken() {
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(regRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Username is already taken.");
    }

    @Test
    void register_EmailAlreadyTaken() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(regRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Email is already taken.");
    }

    // - Login -

    @Test
    void login_UserAuthenticated() {
        when(userRepository.findByUsernameOrEmail(username, username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(logRequest.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
        when(refreshTokenService.issue(any(User.class), any(Instant.class))).thenReturn("refreshToken");

        AuthResponse response = authService.login(logRequest);

        verify(jwtService).generateToken(any(User.class));
        verify(refreshTokenService).issue(any(User.class), any(Instant.class));
        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
    }

    @Test
    void login_UsernameNotFound() {
        when(userRepository.findByUsernameOrEmail(username, username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(logRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials.");
    }

    @Test
    void login_PasswordDoesNotMatch() {
        when(userRepository.findByUsernameOrEmail(username, username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(logRequest.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(logRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials.");
    }

    // - Refresh -

    @Test
    void refresh_TokensRotateSuccessfully() {
        when(refreshTokenService.validateAndRevoke(oldRefreshToken)).thenReturn(refreshContext);
        when(jwtService.generateToken(refreshContext.user())).thenReturn("token");
        when(refreshTokenService.issue(refreshContext.user(), refreshContext.absoluteExpiresAt())).thenReturn("refreshToken");

        AuthResponse response = authService.refresh(refreshRequest);

        verify(refreshTokenService).validateAndRevoke(oldRefreshToken);
        verify(jwtService).generateToken(refreshContext.user());
        verify(refreshTokenService).issue(refreshContext.user(), refreshContext.absoluteExpiresAt());
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.role()).isEqualTo(user.getRole());
    }

    // - Helper -

    private User buildUser(Long id, String email, String username, String password) {
        User u = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(Role.USER)
                .build();

        setField(u, "id", id);
        return u;
    }
}
