package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(min = 8)
        String username,

        @NotBlank
        @Size(min = 8)
        String password
) {}
