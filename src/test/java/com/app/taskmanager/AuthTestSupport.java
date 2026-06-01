package com.app.taskmanager;

import com.app.taskmanager.dto.request.RegisterRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
public class AuthTestSupport {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    public AuthResponse register(String usernamePrefix) throws Exception {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        RegisterRequest request = new RegisterRequest(usernamePrefix + uid, "Password1", usernamePrefix + uid + "@example.com");

        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(body, AuthResponse.class);
    }

    public String bearer(String token) {
        return "Bearer " + token;
    }
}
