package com.app.taskmanager.controller;

import com.app.taskmanager.dto.request.TaskRequest;
import com.app.taskmanager.dto.request.TaskUpdateRequest;
import com.app.taskmanager.dto.response.TaskResponse;
import com.app.taskmanager.enums.TaskStatus;
import com.app.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(taskRequest));
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTasks(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<TaskResponse>> getTasksFiltered(@RequestParam(required = false) String title,
                                               @RequestParam(required = false) TaskStatus status,
                                               Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTasksFiltered(title, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTask(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@Valid @RequestBody TaskUpdateRequest taskUpdateRequest, @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.updateTask(taskUpdateRequest, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}