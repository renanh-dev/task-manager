package com.example.taskmanager.security;

import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private String username = "John";
    private String password = "JohnPass";
    private String email = "John@johnmail.com";

    private User user;

    @BeforeEach
    void setUp() {
        user = buildUser(1L, username, password, email);
    }

    @Test
    void loadUserByUsername_UserIsLoaded() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertThat(userDetails.getUsername()).isEqualTo(username);
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: " + username);
    }

    // - Helper -

    private User buildUser(Long id, String username, String password, String email) {
        User u = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .role(Role.USER)
                .build();

        setField(u, "id", id);
        return u;
    }
}
