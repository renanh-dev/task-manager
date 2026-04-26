package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.TaskRequest;
import com.app.taskmanager.dto.response.TaskResponse;
import com.app.taskmanager.entity.Task;
import com.app.taskmanager.enums.TaskStatus;
import com.app.taskmanager.exception.ResourceNotFoundException;
import com.app.taskmanager.exception.UnauthorizedException;
import com.app.taskmanager.metrics.AppMetrics;
import com.app.taskmanager.repository.TaskRepository;
import com.app.taskmanager.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static com.app.taskmanager.util.TransactionUtils.afterCommit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final AuthUtils authUtils;
    private final AppMetrics appMetrics;

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(Long ownerId, Pageable pageable) {
        return taskRepository.findByOwnerId(ownerId, pageable)
                .map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (!validateOwnership(task))
            throw new UnauthorizedException("Could not get task: Access denied.");

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest taskRequest) {
        Task task = Task.builder()
                .title(taskRequest.title())
                .description(taskRequest.description())
                .owner(authUtils.getCurrentUser())
                .status(TaskStatus.TODO)
                .build();

        taskRepository.save(task);

        afterCommit(appMetrics::recordTaskCreation);

        return TaskResponse.from(task);
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

    @Transactional
    public TaskResponse updateCompletionStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (!validateOwnership(task))
            throw new UnauthorizedException("Could not update task: Access denied.");

        task.markStatus(status);

        taskRepository.save(task);

        return TaskResponse.from(task);
    }

    private boolean validateOwnership(Task task) {
        return task.getOwner().getId().equals(authUtils.getCurrentUser().getId());
    }
}
