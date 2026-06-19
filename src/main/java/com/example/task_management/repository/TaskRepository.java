package com.example.task_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_management.model.Task;
import com.example.task_management.model.User;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignedTo(User user);

    List<Task> findByStatusNot(String status);

    List<Task> findByStatus(String status);

    List<Task> findByCreatedBy(User user);

    List<Task> findByAssignedTo_Role(String role);

    // Employee -> Employee tasks only
    List<Task> findByCreatedBy_RoleAndAssignedTo_Role(
            String createdByRole,
            String assignedToRole);

    Optional<Task> findByTitleIgnoreCaseAndDescriptionIgnoreCase(
            String title,
            String description);
}