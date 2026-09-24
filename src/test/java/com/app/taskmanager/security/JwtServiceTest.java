package com.app.taskmanager.security;

import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private static final String secret = "dGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3Nlc09ubHkxMjM0NTY3ODk=";

    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        setField(jwtService, "secret", secret);
        setField(jwtService, "expiration", 84600000L);

        user = User.builder()
                .username("John")
                .email("John@email.com")
                .password("JohnPassword")
                .role(Role.USER)
                .build();

        setField(user, "id", 1L);
    }

    @Test
    void generateToken_createsNonBlankToken() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractId_returnsCorrectSubjectFromToken() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractId(token)).isEqualTo(user.getId());
    }

    @Test
    void isTokenValid_returnsTrue_forCorrectUserAndFreshToken() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forExpiredToken() {
        setField(jwtService, "expiration", 0L);

        String expiredToken = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void extractId_throwsException_forTamperedToken() {
        String token = jwtService.generateToken(user);
        String tamperedToken = token + "badString";

        assertThatThrownBy(() -> jwtService.extractId(tamperedToken))
                .isInstanceOf(Exception.class);
    }
}
