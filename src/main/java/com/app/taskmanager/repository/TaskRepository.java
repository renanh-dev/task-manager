package com.app.taskmanager.repository;

import com.app.taskmanager.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Not strictly needed for it to function it extends JpaRepository but leave it anyway.
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findTasksByOwnerId(Long ownerId, Pageable pageable);
}
