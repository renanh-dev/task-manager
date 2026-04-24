package com.app.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank
        @Size(min = 3, max = 20)
        String title,

        @Size(max = 500)
        String description
) {}
