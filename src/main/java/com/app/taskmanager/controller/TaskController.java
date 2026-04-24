package com.app.taskmanager.controller;

import com.app.taskmanager.dto.request.TaskRequest;
import com.app.taskmanager.dto.response.TaskResponse;
import com.app.taskmanager.enums.TaskStatus;
import com.app.taskmanager.security.AuthUtils;
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
    private final AuthUtils authUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(taskRequest);
    }

    @GetMapping
    public Page<TaskResponse> getTasks(Pageable pageable) { // Page for returning, Pageable for requesting.
        Long ownerId = authUtils.getCurrentUser().getId();
        return taskService.getTasks(ownerId, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PatchMapping("/{id}/completion")
    public TaskResponse updateCompletionStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        return taskService.updateCompletionStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}

// add filtering of tasks by conditions