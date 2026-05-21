package com.app.taskmanager.security;

import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class JwtServiceTest {
    private JwtService jwtService;
    private static final String SECRET = "dGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3Nlc09ubHkxMjM0NTY3ODk=";
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        setField(jwtService, "secret", SECRET);
        setField(jwtService, "expiration", 84600000L);

        testUser = User.builder()
                .username("John")
                .email("John@email.com")
                .password("JohnPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void generateToken_createsNonBlankToken() {
        String token = jwtService.generateToken(testUser);              // generate token
                                                                        // check if it's not blank
        assertThat(token).isNotBlank();                                 // check if it is separated 3 times by .
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_returnsCorrectSubjectFromToken() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.extractUsername(token)).isEqualTo(testUser.getUsername());
    }

    @Test
    void isTokenValid_returnsTrue_forCorrectUserAndFreshToken() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateToken(testUser);

        User mockUser = User.builder()
                .username("Anna")
                .email("Anna@email.com")
                .password("AnnaPassword")
                .role(Role.USER)
                .build();

        assertThat(jwtService.isTokenValid(token, mockUser)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forExpiredToken() {
        setField(jwtService, "expiration", 0L); // token is born expired for testing.

        String expiredToken = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenValid(expiredToken, testUser)).isFalse();
    }

    @Test
    void extractUsername_throwsException_forTamperedToken() {
        String token = jwtService.generateToken(testUser);
        String tamperedToken = token + "badString";

        assertThatThrownBy(() -> jwtService.extractUsername(tamperedToken))
                .isInstanceOf(Exception.class);
    }
}
