package com.example.taskmanager.service;

import org.springframework.stereotype.Service;

@Service
public class TaskService {
    // try adding rules to certain requests made in controller
    public String print() {
        return "print";
    }
}
