package com.example.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity // This tells Spring Data JPA this should be mapped to the Database.
        /* arguably better to use @Getter and @Setter along with @NoArgsConstructor and @AllArgsConstructor because
         of hashCode() and equals() causing issues with Database relationships */
@Getter
@Setter
@Table(name = "tasks")
@NoArgsConstructor

public class Task { // You only use the keyword "new" (create objects) for entities and DTOs (Data Transfer Objects) in @Service.
    @Id // This tells Spring Data JPA that the variable "long id" is the primary key identifier of a Task object in the Database.

    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment. Logic for increasing number of IDs.
    @Setter(AccessLevel.NONE)
    private Long id;
    private String title;
    private String description;
    private boolean completed;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.completed = false;
    }
    /*
       @AllArgsConstructor -> includes id in the constructor, not optimal for this case since it contains logic.
       @RequiredArgsConstructor -> generates a constructor with final fields, absolutely not optimal for entities.
       @NoArgsConstructor -> ideal in entities since JPA needs one empty constructor to instantiate objects from DB.

       In real world situations builder() is preferred.
    */

}
