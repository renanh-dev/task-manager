package com.app.taskmanager.controller;

import com.app.taskmanager.AuthTestSupport;
import com.app.taskmanager.BaseIntegrationTest;
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


}
