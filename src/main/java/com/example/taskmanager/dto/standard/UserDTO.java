package com.example.taskmanager.dto.standard;

public record UserDTO(
        Long id,
        String username,
        String password,
        String role
) {}
