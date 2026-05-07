package com.app.taskmanager.service;

import com.app.taskmanager.dto.response.UserResponse;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final AuthUtils authUtils;

    @Transactional
    public void deleteOwnUser() {
        User user = authUtils.getCurrentUser();
        user.softDelete();

        userRepository.save(user);
        log.info("User deleted, username={}", user.getUsername());
    }

    public UserResponse getCurrentUser() {
        return UserResponse.from(authUtils.getCurrentUser());
    }
}