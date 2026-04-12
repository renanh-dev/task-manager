package com.example.taskmanager.dto.response;

import java.time.ZonedDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role
) {}
