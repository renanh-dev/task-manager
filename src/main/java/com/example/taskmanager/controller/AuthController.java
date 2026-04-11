package com.example.taskmanager.controller;

import com.example.taskmanager.dto.request.LoginRequest;
import com.example.taskmanager.dto.request.RegisterRequest;
import com.example.taskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest registerRequest) { // @Valid is CRUCIAL for the annotations inside RegisterRequest to work.
        userService.register(registerRequest);
    }

    @PostMapping("/login")
    public void login(@Valid @RequestBody LoginRequest loginRequest) { //mudar
        userService.login(loginRequest);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
