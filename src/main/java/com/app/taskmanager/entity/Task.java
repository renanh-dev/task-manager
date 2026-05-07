package com.app.taskmanager.entity;

import com.app.taskmanager.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@Table(name = "tasks")
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL") // Hibernate automatically appends this to every query, excludes soft deleted tasks
@EntityListeners(AuditingEntityListener.class)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Avoid EAGER loading, causes N+1 query issue.
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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;

    @Builder
    public Task(String title, String description, User owner, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.owner = owner;
        this.taskStatus = status;
    }

    public void updateTitle(String title) {
        setTitle(title);
    }

    public void updateDescription(String description) {
        setDescription(description);
    }

    public void updateStatus(TaskStatus s) {
        setTaskStatus(s);
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }
}

// indexing necessary for querying