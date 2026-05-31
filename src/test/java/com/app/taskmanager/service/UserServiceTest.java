package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.UserUpdateRequest;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.exception.InvalidCredentialsException;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthUtils authUtils;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("John")
                .email("john@email.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        setField(user, "id", 1L);
        when(authUtils.getCurrentUser()).thenReturn(user);
    }

    @Test
    void changeCredentials_UpdatesUsername() {
        UserUpdateRequest request = new UserUpdateRequest("newUsername", null, null);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.existsByUsername("newUsername")).thenReturn(false);

        userService.changeCredentials(request);

        assertThat(user.getUsername()).isEqualTo("newUsername");
        verify(userRepository).save(user);
    }

    @Test
    void changeCredentials_UpdatesEmail() {
        UserUpdateRequest request = new UserUpdateRequest(null, null, "new@email.com");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);

        userService.changeCredentials(request);

        assertThat(user.getEmail()).isEqualTo("new@email.com");
        verify(userRepository).save(user);
    }

    @Test
    void changeCredentials_UpdatesPassword() {
        UserUpdateRequest request = new UserUpdateRequest(null, "newPassword", null);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        userService.changeCredentials(request);

        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        verify(userRepository).save(user);
    }

    @Test
    void changeCredentials_ThrowsException_UsernameAlreadyTaken() {
        UserUpdateRequest request = new UserUpdateRequest("takenUsername", null, null);
        when(userRepository.existsByUsername("takenUsername")).thenReturn(true);

        assertThatThrownBy(() -> userService.changeCredentials(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Username is already taken.");
    }

    @Test
    void changeCredentials_ThrowsException_EmailAlreadyTaken() {
        UserUpdateRequest request = new UserUpdateRequest(null, null, "taken@email.com");
        when(userRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.changeCredentials(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email is already taken.");
    }

    @Test
    void changeCredentials_AllowsSameUsername() {
        UserUpdateRequest request = new UserUpdateRequest(user.getUsername(), null, null);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        assertThatCode(() -> userService.changeCredentials(request))
                .doesNotThrowAnyException();
    }

    @Test
    void changeCredentials_AllowsSameEmail() {
        UserUpdateRequest request = new UserUpdateRequest(null, null, user.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThatCode(() -> userService.changeCredentials(request))
                .doesNotThrowAnyException();
    }
}