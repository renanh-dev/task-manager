package com.example.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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
    @Column(nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    /*
        Many tasks to one user. Fetch type LAZY is standard unless you need EAGER for some reason. Even then it's not recommended.
        EAGER makes an additional query for the user, which is also bad due to the N+1 problem.
    */
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    private String description;

    public Task(String title, String description, User owner) {
        this.title = title;
        this.description = description;
        this.owner = owner;
        this.completed = false;
    }
    /*
       @AllArgsConstructor -> includes id in the constructor, not optimal for this case since it contains logic.
       @RequiredArgsConstructor -> generates a constructor only including final fields, absolutely not optimal for entities.
       @NoArgsConstructor -> ideal in entities since JPA needs one empty constructor to instantiate objects from DB.

       In real world situations builder() is preferred.
    */

}
