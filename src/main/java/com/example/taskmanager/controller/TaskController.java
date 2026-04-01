package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<Task> getAll() {
        return service.listAllTasks();
    }

    @PostMapping
    public void create(@RequestBody Task task) {
        service.saveTask(task);
    }

    @GetMapping("/complete")
    public List<Task> getComplete() {
        return service.filter(t -> t.isCompleted());
    }

    @GetMapping("/cocoduro")
    public void fazalgumacoisa() {
        service.executeIfComplete(task, t -> System.out.println("Finalizada: " + t.getTitle()));
    }
}
