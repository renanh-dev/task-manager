package com.app.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TaskManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args); // 99% of cases it's never altered from this.
    }
}

/*
    -global exception handling
    -logging
    -API documentation
    -pagination
    -rate limiting
    caching
    -metrics
    -monitoring
*/
