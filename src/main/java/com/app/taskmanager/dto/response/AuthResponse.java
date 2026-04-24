package com.app.taskmanager.dto.response;

import com.app.taskmanager.enums.Role;

public record AuthResponse(
        String token,
        String username,
        Role role
) {}
