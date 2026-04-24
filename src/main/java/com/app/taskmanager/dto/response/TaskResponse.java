package com.app.taskmanager.dto.response;

import com.app.taskmanager.enums.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status
) {}
