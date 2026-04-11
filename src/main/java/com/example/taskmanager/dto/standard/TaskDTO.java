package com.example.taskmanager.dto.standard;

public record TaskDTO(
        Long id,
        String title,
        String description,
        boolean completed
) {}
