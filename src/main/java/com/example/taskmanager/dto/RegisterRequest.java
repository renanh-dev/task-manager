package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 8)
        String username,

        @NotBlank
        @Size(min = 8)
        String password
) {}

// I know they're the same, maybe some difference in future updates?
