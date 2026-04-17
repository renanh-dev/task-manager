package com.example.taskmanager.security;

import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Role;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceTest {
    private JwtService jwtService;
    private final String secret = "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2g=";
    private final Long expiration = 84600000L;


    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);
    }

    @Test
    void generateToken_extractedUsernameShouldMatchOriginal() {
        User user = User.builder()
                .username("john")
                .role(Role.USER)
                .build();

        Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims()
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {

    }
}
