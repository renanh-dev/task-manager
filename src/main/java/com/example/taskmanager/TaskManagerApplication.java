package com.example.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args); // 99% of cases it's never altered from this.
    }
}

/* OBJECTIVE OF THE APP: Experiment with CRUD (Create, Read, Update, Delete). Make specific URLs manipulate tasks.
Real systems usually expand this by having all of these:
    global exception handling
    logging
    API documentation
    pagination
    rate limiting
    caching
    metrics
    monitoring
* */
