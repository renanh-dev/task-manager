package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> listAllTasks() {
        return repository.findAll();
    }

    public void saveTask(Task task) {
        repository.save(task);
    }

    public List<Task> filter(Predicate<Task> rule) {
        return repository.findAll()
                .stream()
                .filter(rule)
                .toList();
    }

    public void executeIfComplete(Task task, Consumer<Task> action) {
        if (task.isCompleted()) {
            action.accept(task);
        }
    }

}
