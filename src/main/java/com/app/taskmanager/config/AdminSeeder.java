package com.app.taskmanager.config;

import com.app.taskmanager.entity.User;
import com.app.taskmanager.enums.Role;
import com.app.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${ADMIN_USERNAME:#{null}}")
    private String adminUsername;

    @Value("${ADMIN_EMAIL:#{null}}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD_BCRYPT:#{null}}")
    private String adminPasswordBcrypt;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminEmail == null || adminPasswordBcrypt == null) {
            log.info("Admin seed credentials not configured, skipping");
            return;
        }

        if (userRepository.existsByUsernameOrEmail(adminUsername, adminEmail)) {
            log.info("Admin user already exists, skipping seed");
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .password(adminPasswordBcrypt)
                .email(adminEmail)
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Admin user saved, username={}", adminUsername);
    }
}
