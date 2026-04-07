package com.example.taskmanager.dto;

public record UserDTO(
        Long id,
        String username,
        String password,
        String role
) {}
