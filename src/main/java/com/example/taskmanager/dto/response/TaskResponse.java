package com.example.taskmanager.dto.response;

public record TaskResponse(
        Long id,
        String title,
        String description,
        boolean completed
) {}
