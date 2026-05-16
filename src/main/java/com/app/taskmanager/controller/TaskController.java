package com.app.taskmanager.controller;

import com.app.taskmanager.dto.request.TaskRequest;
import com.app.taskmanager.dto.request.TaskUpdateRequest;
import com.app.taskmanager.dto.response.TaskResponse;
import com.app.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(taskRequest);
    }

    @GetMapping
    public Page<TaskResponse> getTasks(Pageable pageable) { // Page for returning, Pageable for requesting.
        return taskService.getTasks(pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PatchMapping("/{id}")
    public TaskResponse updateTask(@Valid @RequestBody TaskUpdateRequest taskUpdateRequest, @PathVariable Long id) {
        return taskService.updateTask(taskUpdateRequest, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}