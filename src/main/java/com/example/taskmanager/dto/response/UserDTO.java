package com.example.taskmanager.dto.response;

public record UserDTO(
        Long id,
        String username,
        String password,
        String role
) {}
