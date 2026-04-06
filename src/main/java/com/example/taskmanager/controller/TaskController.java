package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController /*
                    It takes a "Web Request" (which is just a bunch of text traveling over a wire) and turns it into a Java Method call.
                    It then takes your Java Object and turns it back into JSON text for the other computer to read.
                    Controller should only know how to relay data, never anything else like dealing with entities or DTOs.

                    @GetMapping equals fetching data.
                    @PostMapping equals sending data.
                    @PutMapping equals updating/replacing existing data.
                    @DeleteMapping equals removing data.
                    @PatchMapping equals Partial Update, changing just one field.
                */
@RequestMapping("/api")
public class TaskController {
    @Autowired
    private TaskRepository repository;
    @Autowired
    private TaskService service;

    @GetMapping
    public Task something() {
        return new Task();
    }

    @GetMapping("/print")
    public String print() {
        return service.print();
    }

    @GetMapping("/allTasks")
    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    @PostMapping("/createTask")
    public Task createTask(@RequestBody Task task) { /* @RequestBody tells Spring the data is being received from a JSON,
                                                        and it automatically parses it into a Java Object.             */
        return repository.save(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) { /* @PathVariable tells Spring the data is being received from the URL
                                                       itself and parses it into a normal variable like "Long id"      */
        repository.deleteById(id);
    }
}
