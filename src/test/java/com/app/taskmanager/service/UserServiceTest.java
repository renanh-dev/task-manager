package com.app.taskmanager.service;

import com.app.taskmanager.dto.response.UserResponse;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private UserService userService;

    private User user;

    private String username = "John";
    private String password = "JohnPass";
    private String email = "John@johnmail.com";

    @BeforeEach
    void setUp() {
        user = buildUser(1L, email, username, password);
    }

    @Test
    void getCurrentUser_UserIsReturnedSuccessfully() {
        when(authUtils.getCurrentUser()).thenReturn(user);

        assertThat(userService.getCurrentUser()).isEqualTo(UserResponse.from(user));
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
