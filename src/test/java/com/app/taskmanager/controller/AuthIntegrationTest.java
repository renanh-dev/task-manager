package com.app.taskmanager.controller;

import com.app.taskmanager.AuthTestSupport;
import com.app.taskmanager.BaseIntegrationTest;
import com.app.taskmanager.dto.request.LoginRequest;
import com.app.taskmanager.dto.request.RefreshTokenRequest;
import com.app.taskmanager.dto.request.RegisterRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthIntegrationTest extends BaseIntegrationTest {

    private AuthTestSupport auth;

    @BeforeEach
    void setUp() {
        auth = new AuthTestSupport(mockMvc, objectMapper);
    }

    @Test
    void register_withValidRequest_returnsTokensAndUserInfo() throws Exception {
        RegisterRequest request = new RegisterRequest("Alice01", "Password1", "Alice01@example.com");

        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("Alice01"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn().getResponse().getContentAsString();

        AuthResponse response = objectMapper.readValue(body, AuthResponse.class);
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    void register_withDuplicateUsername_returns401() throws Exception {
        RegisterRequest request1 = new RegisterRequest("fixedUser1", "Password1", "fixedUser1@example.com");
        RegisterRequest request2 = new RegisterRequest("fixedUser1", "Password1", "fixedUser2@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withDuplicateEmail_returns401() throws Exception {
        RegisterRequest request1 = new RegisterRequest("fixedUser1", "Password1", "fixedUser1@example.com");
        RegisterRequest request2 = new RegisterRequest("fixedUser2", "Password1", "fixedUser1@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withInvalidPassword_returns401() throws Exception {
        RegisterRequest req = new RegisterRequest("validuser1", "password1", "valid1@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withInvalidUsername_returns401() throws Exception {
        RegisterRequest req = new RegisterRequest("bad user!", "Password1", "valid2@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // - Login -

    @Test
    void login_withValidCredentials_returnsTokens() throws Exception {
        RegisterRequest reg = new RegisterRequest("loginuser1", "Password1", "loginuser1@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("loginuser1", "Password1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("loginuser1"));
    }

    @Test
    void login_canUseEmailAsIdentifier() throws Exception {
        RegisterRequest reg = new RegisterRequest("emaillogin1", "Password1", "emaillogin1@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("emaillogin1@example.com", "Password1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("emaillogin1"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        RegisterRequest reg = new RegisterRequest("wrongpass1", "Password1", "wrongpass1@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("wrongpass1", "WrongPassword1"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withUnknownUser_returns401() throws Exception {
        LoginRequest login = new LoginRequest("unknown", "Password1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    // - Refresh -

    @Test
    void refresh_withValidRefreshToken_rotatesTokens() throws Exception {
        AuthResponse original = auth.register("refreshuser");

        String body = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(original.refreshToken()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse rotated = objectMapper.readValue(body, AuthResponse.class);
        assertThat(rotated.accessToken()).isNotEqualTo(original.accessToken());
        assertThat(rotated.refreshToken()).isNotEqualTo(original.refreshToken());
    }

    @Test
    void refresh_withConsumedToken_returns401() throws Exception {
        AuthResponse original = auth.register("replayuser");
        RefreshTokenRequest req = new RefreshTokenRequest(original.refreshToken());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // - Logout -


}
