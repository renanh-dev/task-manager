package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController /*
                    It takes a "Web Request" (which is just a bunch of text traveling over a wire) and turns it into a Java Method call.
                    It then takes your Java Object and turns it back into JSON text for the other computer to read.
                    Controller should only know how to relay data, never anything else like dealing with entities or DTOs.
                    It only connects to @Service.

                    @GetMapping    → client requests data from the server (read)
                    @PostMapping   → client sends data to create a new resource
                    @PutMapping    → client replaces an existing resource entirely
                    @PatchMapping  → client partially updates an existing resource
                    @DeleteMapping → client removes a resource

                    @RequestBody tells Spring the data is being received from a JSON, and it automatically parses it into a Java Object.
                    @PathVariable tells Spring the data is being received from the URL itself and parses it into a normal variable like "Long id"
                */
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping // No need to write "createTasks" since "@PostMapping" already tells you what the method does with tasks.
    public TaskDTO createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(taskRequest);
    }

    @GetMapping
    public List<TaskDTO> getTasks() {
        return taskService.getTasks();
    }

    @GetMapping("/{id}")
    public TaskDTO getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @DeleteMapping
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
