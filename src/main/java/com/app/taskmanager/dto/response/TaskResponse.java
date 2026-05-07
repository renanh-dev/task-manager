package com.app.taskmanager.dto.response;

import com.app.taskmanager.entity.Task;
import com.app.taskmanager.enums.TaskStatus;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getTaskStatus(), task.getCreatedAt(), task.getUpdatedAt());
    }
}
