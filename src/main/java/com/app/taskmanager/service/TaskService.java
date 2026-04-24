package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.TaskRequest;
import com.app.taskmanager.dto.response.TaskResponse;
import com.app.taskmanager.entity.Task;
import com.app.taskmanager.enums.TaskStatus;
import com.app.taskmanager.exception.ResourceNotFoundException;
import com.app.taskmanager.exception.UnauthorizedException;
import com.app.taskmanager.repository.TaskRepository;
import com.app.taskmanager.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final AuthUtils authUtils;

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(Long ownerId, Pageable pageable) {
        return taskRepository.findByOwnerId(ownerId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (!validateOwnership(task))
            throw new UnauthorizedException("Could not get task: Access denied.");

        return mapToDTO(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest taskRequest) {
        Task task = new Task(taskRequest.title(), taskRequest.description(), authUtils.getCurrentUser());

        return mapToDTO(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (!validateOwnership(task)) {
            throw new UnauthorizedException("Could not delete task: Access denied.");
        }

        task.softDelete();
        taskRepository.save(task);
    }

    private TaskResponse mapToDTO(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getTaskStatus());
    }

    @Transactional
    public TaskResponse updateCompletionStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (!validateOwnership(task))
            throw new UnauthorizedException("Could not update task: Access denied.");

        task.markStatus(status);

        return mapToDTO(taskRepository.save(task));
    }

    private boolean validateOwnership(Task task) {
        return task.getOwner().getId().equals(authUtils.getCurrentUser().getId());
    }
}
