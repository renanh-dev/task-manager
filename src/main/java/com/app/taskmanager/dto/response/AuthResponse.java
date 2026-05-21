package com.app.taskmanager.dto.response;

import com.app.taskmanager.enums.Role;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String username,
        Role role
) {}
