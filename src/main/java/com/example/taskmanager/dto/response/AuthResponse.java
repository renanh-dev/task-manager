package com.example.taskmanager.dto.response;

public record AuthResponse(
        String token,
        String username,
        String role
) {}
