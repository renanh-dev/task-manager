package com.example.taskmanager.entity;

import com.example.taskmanager.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity // This tells Spring Data JPA this should be mapped to the Database.
        /* arguably better to use @Getter and @Setter along with @NoArgsConstructor and @AllArgsConstructor because
         of hashCode() and equals() causing issues with Database relationships */
@Getter
@Setter(AccessLevel.PRIVATE)
@Table(name = "tasks")
@SoftDelete(strategy = SoftDeleteType.DELETED) // Soft deleting is standard.
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false)
    private TaskStatus taskStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    private String description;

    @Version // solves concurrency problems
    private Long version;

    public Task(String title, String description, User owner) {
        this.title = title;
        this.description = description;
        this.owner = owner;
        this.taskStatus = TaskStatus.TODO;
    }

    public void markStatus(TaskStatus s) {
        setTaskStatus(s); // change this to check if transition is possible
    }

    /*
       @AllArgsConstructor -> includes id in the constructor, not optimal for this case since it contains logic.
       @RequiredArgsConstructor -> generates a constructor only including final fields, absolutely not optimal for entities.
       @NoArgsConstructor -> ideal in entities since JPA needs one empty constructor to instantiate objects from DB.

       In real world situations builder() is preferred.
    */

}

// indexing necessary for querying