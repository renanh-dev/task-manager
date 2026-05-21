package com.app.taskmanager.dto.context;

import com.app.taskmanager.entity.User;

import java.time.Instant;

public record RefreshTokenContext(
        User user,
        Instant absoluteExpiresAt
) {}
