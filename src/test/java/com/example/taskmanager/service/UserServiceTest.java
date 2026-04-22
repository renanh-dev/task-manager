package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.RegisterRequest;
import com.example.taskmanager.dto.request.LoginRequest;
import com.example.taskmanager.dto.response.AuthResponse;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.exception.InvalidCredentialsException;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.AuthUtils;
import com.example.taskmanager.security.JwtService;
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
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private UserService userService;

    private RegisterRequest regRequest;
    private LoginRequest logRequest;
    private User user;

    private String username = "John";
    private String password = "JohnPass";
    private String email = "John@johnmail.com";

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

        AuthResponse response = userService.register(regRequest);

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        assertThat(response.token()).isEqualTo("token");
    }

    @Test
    void register_UserAlreadyTaken() {
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThatThrownBy(() -> userService.register(regRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Username is already taken.");
    }

    @Test
    void register_EmailAlreadyTaken() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> userService.register(regRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email is already taken.");
    }

    // - Login -

    @Test
    void login_UserAuthenticated() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(logRequest.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        userService.login(logRequest);

        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void login_UsernameNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(logRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials.");
    }

    @Test
    void login_PasswordDoesNotMatch() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(logRequest.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.login(logRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials.");
    }

    // - Delete -

    @Test
    void deleteOwnUser_UserIsDeleted() {
        when(authUtils.getCurrentUser()).thenReturn(user);

        userService.deleteOwnUser();

        verify(userRepository).save(user);
        assertThat(user.getDeletedAt()).isNotNull();
    }

    // - Helpers -

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
