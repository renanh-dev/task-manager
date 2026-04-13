package com.example.taskmanager.controller;

import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

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
    //@ResponseBody not needed since it's a @RestController and not @Controller.
    public TaskResponse createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(taskRequest);
    }

    /*
        Pageable = input (what slice of data the client wants: page number, size, sort)
        Page = output (the results + metadata: total elements, total pages, etc.)

        Standard for any list endpoint that can grow over time.
    */

    /*@GetMapping
    public Page<TaskResponse> getTasks(Pageable pageable) { // Page for returning, Pageable for requesting.
        Long ownerId = // get from security once auth is wired.
        return taskService.getTasks(ownerId, pageable);
    }

*/

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PatchMapping("/{id}/completion")
    public void updateCompletionStatus(@PathVariable Long id, @RequestParam boolean completed) { // @RequestParam captures query parameters, a field of task here.
        taskService.updateCompletionStatus(id, completed);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
