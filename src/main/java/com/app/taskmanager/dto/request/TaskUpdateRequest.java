package com.app.taskmanager.dto.request;

import com.app.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
        @Size(min = 3, max = 100)
        String title,

        @Size(max = 500)
        String description,

        TaskStatus status
)
{
        @AssertTrue(message = "At least one field must be provided")
        public boolean isAtLeastOneFieldPresent() {
                return title != null || description != null || status != null;
        }
}
