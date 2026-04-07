package com.example.taskmanager.dto;

public record TaskDTO(
        Long id,
        String title,
        String description,
        boolean completed
) {}
