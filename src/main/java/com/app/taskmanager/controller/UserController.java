package com.app.taskmanager.controller;

import com.app.taskmanager.dto.request.UserUpdateRequest;
import com.app.taskmanager.dto.response.UserResponse;
import com.app.taskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }

    @PatchMapping("/me")
    public UserResponse changeCredentials(@Valid @RequestBody UserUpdateRequest request) {
        return userService.changeCredentials(request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwnUser() {
        userService.deleteOwnUser();
    }
}
