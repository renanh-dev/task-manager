package com.app.taskmanager.dto.response;

import com.app.taskmanager.entity.Task;
import com.app.taskmanager.enums.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getTaskStatus());
    }
}
