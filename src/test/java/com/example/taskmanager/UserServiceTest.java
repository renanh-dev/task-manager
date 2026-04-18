package com.example.taskmanager;

import com.example.taskmanager.dto.request.RegisterRequest;
import com.example.taskmanager.dto.request.LoginRequest;
import com.example.taskmanager.dto.response.AuthResponse;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.AuthUtils;
import com.example.taskmanager.security.JwtService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private RegisterRequest goodRegRequest;
    private LoginRequest goodLogRequest;
    private User user;

    private String username = "John";
    private String password = "JohnPass";
    private String email = "John@johnmail.com";

    @BeforeEach
    void setUp() {
        goodRegRequest = new RegisterRequest(username, password, email);
        goodLogRequest = new LoginRequest(username, password);

        user = buildUser(1L, email, username, password);
    }

    // - Register -

    @Test
    void register_UserIsCreatedAndSaved() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        userService.register(goodRegRequest);

        verify(userRepository).save(any(User.class));
    }

    // - Helpers -

    private User buildUser(Long id, String email, String username, String password) {
        User u = User.builder()
                .username(username)
                .email(email)
                .password("password")
                .role(Role.USER)
                .build();

        setField(u, "id", id);
        return u;
    }
}
