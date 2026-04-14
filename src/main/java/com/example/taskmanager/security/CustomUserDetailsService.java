package com.example.taskmanager.security;

import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    /*
        This class connects a user from the database and Spring Security's own concept of a user.
        It takes a user from the database and converts it into a form Spring Security understands.
    */

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .disabled(!user.isEnabled())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
