package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.LoginRequest;
import com.app.taskmanager.dto.request.RegisterRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.metrics.AppMetrics;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AppMetrics appMetrics;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest regRequest;
    private LoginRequest logRequest;

    private String username = "John";
    private String password = "JohnPass";
    private String email = "John@johnmail.com";

    private User user;

    @BeforeEach
    void setUp() {
        regRequest = new RegisterRequest(username, password, email);
        logRequest = new LoginRequest(username, password);

        user = buildUser(1L, email, username, password);
    }

    // - Register -

    @Test
    void register_UserIsCreatedAndSaved() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        AuthResponse response = authService.register(regRequest);

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        assertThat(response.accessToken()).isEqualTo("token");
    }

    @Test
    void register_UserAlreadyTaken() {
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(regRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Username is already taken.");
    }

    @Test
    void register_EmailAlreadyTaken() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(regRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email is already taken.");
    }

    // - Login -

    @Test
    void login_UserAuthenticated() {
        when(userRepository.findByUsernameOrEmail(username, username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(logRequest.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        authService.login(logRequest);

        verify(jwtService).generateToken(any(User.class));
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
