package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Not strictly needed for it to function it extends JpaRepository but leave it anyway.
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByOwnerId(Long ownerId, Pageable pageable);

    /*
        It is an interface because Spring automatically creates a class (or proxy) in runtime that implements this with the necessary methods filled out.
        It extends JpaRepository, when an interface extends another it inherits all method contracts from the parent interface.

        You are telling Spring: "Here is my @Entity (Task) and its @Id type (Long). Please generate all the methods containing database logic for me."
        It gives you standard methods for using the database by abstracting the SQL away.
        You can also create methods and Spring will make the SQL code for you based on keywords.
    */
}
