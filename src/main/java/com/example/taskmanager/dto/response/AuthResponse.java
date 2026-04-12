package com.example.taskmanager.dto.response;

import com.example.taskmanager.enums.Role;

public record AuthResponse(
        String token,
        String username,
        Role role
) {}
