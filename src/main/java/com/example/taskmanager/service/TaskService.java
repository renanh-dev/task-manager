package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.response.TaskDTO;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public List<TaskDTO> getTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public TaskDTO getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        return mapToDTO(task);
    }

    public TaskDTO createTask(TaskRequest taskRequest) {
        Task task = new Task(taskRequest.title(), taskRequest.description());

        Task savedTask = taskRepository.save(task); // It returns the complete Task object with an assigned ID.

        return mapToDTO(savedTask);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    private TaskDTO mapToDTO(Task task) {
        return new TaskDTO(task.getId(), task.getTitle(), task.getDescription(), task.isCompleted());
    }

    public void updateCompletionStatus(Long id, boolean completed) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        task.setCompleted(completed);
        taskRepository.save(task);
    }
}
