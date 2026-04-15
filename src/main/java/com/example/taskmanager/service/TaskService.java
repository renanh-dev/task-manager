package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.exception.UnauthorizedException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final AuthUtils authUtils;

    public Page<TaskResponse> getTasks(Long ownerId, Pageable pageable) {
        return taskRepository.findByOwnerId(ownerId, pageable)
                .map(this::mapToDTO);
    }

    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (!task.getOwner().getId().equals(authUtils.getCurrentUser().getId()))
            throw new UnauthorizedException("Could not get task: Access denied.");

        return mapToDTO(task);
    }

    public TaskResponse createTask(TaskRequest taskRequest) {
        Task task = new Task(taskRequest.title(), taskRequest.description(), authUtils.getCurrentUser());

        return mapToDTO(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (!task.getOwner().getId().equals(authUtils.getCurrentUser().getId())) {
            throw new UnauthorizedException("Could not delete task: Access denied.");
        }

        taskRepository.deleteById(id);
    }

    private TaskResponse mapToDTO(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.isCompleted());
    }

    public TaskResponse updateCompletionStatus(Long id, boolean completed) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (!task.getOwner().getId().equals(authUtils.getCurrentUser().getId()))
            throw new UnauthorizedException("Could not update task: Access denied.");

        task.setCompleted(completed);

        return mapToDTO(taskRepository.save(task));
    }
}
