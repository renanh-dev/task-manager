package com.example.taskmanager.controller;

import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest registerRequest) { // @Valid is CRUCIAL for the annotations inside RegisterRequest to work.
        userService.register(registerRequest);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.register(registerRequest);
    }
}
