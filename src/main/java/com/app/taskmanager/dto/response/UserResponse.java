package com.app.taskmanager.dto.response;

import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        Instant createdAt
)
{
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
