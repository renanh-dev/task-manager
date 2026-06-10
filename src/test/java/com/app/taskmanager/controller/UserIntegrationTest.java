package com.app.taskmanager.controller;

import com.app.taskmanager.AuthTestSupport;
import com.app.taskmanager.BaseIntegrationTest;
import com.app.taskmanager.dto.request.LoginRequest;
import com.app.taskmanager.dto.request.UserUpdateRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserIntegrationTest extends BaseIntegrationTest {

    private AuthTestSupport auth;
    private AuthResponse currentUser;

    @BeforeEach
    void setUp() throws Exception {
        auth = new AuthTestSupport(mockMvc, objectMapper);
        currentUser = auth.register("usertest");
    }

    // - Get -

    @Test
    void getCurrentUser_returnsOwnProfile() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", auth.bearer(currentUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(currentUser.username()))
                .andExpect(jsonPath("$.email").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getCurrentUser_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }

    // - Patch -

    @Test
    void changeCredentials_updatesUsername() throws Exception {
        mockMvc.perform(patch("/api/user/me")
                        .header("Authorization", auth.bearer(currentUser.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserUpdateRequest("newusername1", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername1"));
    }

    @Test
    void changeCredentials_updatesEmail() throws Exception {
        mockMvc.perform(patch("/api/user/me")
                        .header("Authorization", auth.bearer(currentUser.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserUpdateRequest(null, null, "newemail@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@example.com"));
    }

    @Test
    void changeCredentials_updatesPassword_allowsLoginWithNewPassword() throws Exception {
        mockMvc.perform(patch("/api/user/me")
                        .header("Authorization", auth.bearer(currentUser.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserUpdateRequest(null, "NewPassword2", null))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(currentUser.username(), "Password1"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(currentUser.username(), "NewPassword2"))))
                .andExpect(status().isOk());
    }

    @Test
    void changeCredentials_withNoFields_returns400() throws Exception {
        mockMvc.perform(patch("/api/user/me")
                        .header("Authorization", auth.bearer(currentUser.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeCredentials_withInvalidEmail_returns400() throws Exception {
        mockMvc.perform(patch("/api/user/me")
                        .header("Authorization", auth.bearer(currentUser.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserUpdateRequest(null, null, "not-an-email"))))
                .andExpect(status().isBadRequest());
    }

    // - Delete -

    @Test
    void deleteOwnUser_returns204AndInvalidatesAccess() throws Exception {
        mockMvc.perform(delete("/api/user/me")
                        .header("Authorization", auth.bearer(currentUser.accessToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", auth.bearer(currentUser.accessToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteOwnUser_withoutAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }
}