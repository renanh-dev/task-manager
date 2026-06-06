package com.app.taskmanager.service;

import com.app.taskmanager.dto.request.TaskRequest;
import com.app.taskmanager.dto.request.TaskUpdateRequest;
import com.app.taskmanager.dto.response.TaskResponse;
import com.app.taskmanager.entity.Task;
import com.app.taskmanager.enums.TaskStatus;
import com.app.taskmanager.exception.ResourceNotFoundException;
import com.app.taskmanager.exception.ForbiddenException;
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
    public Page<TaskResponse> getTasks(Pageable pageable) {
        return taskRepository.findTasksByOwnerId(authUtils.getCurrentUser().getId(), pageable)
                .map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksFiltered(String title, TaskStatus status, Pageable pageable) {
        if (title != null && title.isBlank()) title = null;

        Long ownerId = authUtils.getCurrentUser().getId();
        return taskRepository.findByFilters(ownerId, title, status, pageable)
                .map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = findTaskById(id);

        if (isNotOwner(task)) {
            log.warn("Unauthorized task access, taskId={}, userId={}", id, authUtils.getCurrentUser().getId());
            throw new ForbiddenException("Could not get task: Access denied.");
        }

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

        log.info("Task created, taskId={}, ownerId={}, createdAt={}", task.getId(), task.getOwner().getId(), task.getCreatedAt());
        return TaskResponse.from(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskById(id);

        if (isNotOwner(task)) {
            log.warn("Unauthorized task delete, taskId={}, userId={}", id, task.getOwner().getId());
            throw new ForbiddenException("Could not delete task: Access denied.");
        }

        task.softDelete();
        taskRepository.save(task);

        log.info("Task soft deleted, taskId={}, ownerId={}", task.getId(), task.getOwner().getId());
    }

    @Transactional
    public TaskResponse updateTask(TaskUpdateRequest request, Long id) {
        Task task = findTaskById(id);

        if (isNotOwner(task)) {
            log.warn("Unauthorized task update, taskId={}, userId={}", task.getId(), authUtils.getCurrentUser().getId());
            throw new ForbiddenException("Could not update task: Access denied.");
        }

        if (request.title() != null) task.updateTitle(request.title());
        if (request.description() != null) task.updateDescription(request.description());
        if (request.status() != null) task.updateStatus(request.status());

        taskRepository.save(task);

        log.info("Task updated, taskId={}, ownerId={}, updatedAt={}", task.getId(), task.getOwner().getId(), task.getUpdatedAt());
        return TaskResponse.from(task);
    }

    private boolean isNotOwner(Task task) {
        return !task.getOwner().getId().equals(authUtils.getCurrentUser().getId());
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found, taskId={}", id);
                    return new ResourceNotFoundException("Task not found.");
                });
    }
}
