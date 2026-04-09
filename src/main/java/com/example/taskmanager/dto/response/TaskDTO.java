package com.example.taskmanager.dto.response;

public record TaskDTO(
        Long id,
        String title,
        String description,
        boolean completed
) {}
