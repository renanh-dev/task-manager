package com.app.taskmanager.controller;

import com.app.taskmanager.dto.request.UserUpdateRequest;
import com.app.taskmanager.dto.response.UserResponse;
import com.app.taskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getCurrentUser());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> changeCredentials(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.changeCredentials(request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteOwnUser() {
        userService.deleteOwnUser();
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
