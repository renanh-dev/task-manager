package com.app.taskmanager.repository;

import com.app.taskmanager.entity.Task;
import com.app.taskmanager.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findTasksByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
            "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR t.taskStatus = :status)")
    Page<Task> findByFilters(@Param("title") String title,
                             @Param("status") TaskStatus status,  // enum here
                             Pageable pageable);
}
