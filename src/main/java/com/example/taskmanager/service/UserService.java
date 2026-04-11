package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.LoginRequest;
import com.example.taskmanager.dto.request.RegisterRequest;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest registerRequest) {

    }

    public void login(LoginRequest loginRequest) {

    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) { // why UserDetails?

    }
}

// CONTINUE BY USING JWT AS CLAUDE SPECIFIED
