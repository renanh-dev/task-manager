package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    /*
        You are telling Spring: "Here is my @Entity (Task) and its @Id type (Long). Please generate all the database logic for me."
        It gives you standard methods for using the database by abstracting the SQL away.
        You can also create methods and Spring will make the SQL code for you.
    */
}
