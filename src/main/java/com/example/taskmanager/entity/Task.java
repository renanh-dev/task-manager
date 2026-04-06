package com.example.taskmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity // This tells Spring Data JPA this should be mapped to the Database.

@Data /* arguably better to use @Getter and @Setter along with @NoArgsConstructor and @AllArgsConstructor because
         of hashCode() and equals() causing issues with Database relationships */

public class Task { // You only use the keyword "new" for entities and DTOs (Data Transfer Objects) in @Service.
    @Id // This tells Spring Data JPA that the variable "long id" is the primary key identifier of a Task object in the Database.

    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment. Logic for increasing number of IDs.
    private long id;
    private String title;
    private String description;
    private boolean completed;
}
