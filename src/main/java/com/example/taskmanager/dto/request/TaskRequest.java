package com.example.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank
        String title,

        @Size(max = 500)
        String description
) {}
