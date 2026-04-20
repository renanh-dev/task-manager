package com.example.taskmanager.dto.response;

import com.example.taskmanager.enums.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status
) {}
