package com.app.taskmanager.dto.request;

import com.app.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
        @Size(min = 3, max = 100)
        String title,

        @Size(max = 500)
        String description,

        TaskStatus status
){
}
