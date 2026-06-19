package com.example.task_management.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.task_management.model.Task;
import com.example.task_management.model.User;
import com.example.task_management.repository.TaskRepository;
import com.example.task_management.repository.UserRepository;
import com.example.task_management.service.TaskService;

@Controller
public class DashboardController {

        @Autowired
        private TaskRepository taskRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private TaskService taskService;

        @GetMapping("/dashboard")
        public String dashboard(Model model,
                        RedirectAttributes redirectAttributes) {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                String email = auth.getName();

                System.out.println("=================================");
                System.out.println("Logged In Email: " + email);

                User loggedInUser = userRepository.findByEmail(email);

                if (loggedInUser == null) {

                        System.out.println(
                                        "No User Found For Email : " + email);

                        return "redirect:/login";
                }

                model.addAttribute(
                                "userName",
                                loggedInUser.getName());

                model.addAttribute(
                                "userRole",
                                loggedInUser.getRole());

                System.out.println(
                                "Logged In User : "
                                                + loggedInUser.getName());

                System.out.println(
                                "Role : "
                                                + loggedInUser.getRole());

                // ==========================
                // ALL TASKS
                // ==========================

                List<Task> tasks;

                if ("MANAGER".equalsIgnoreCase(
                                loggedInUser.getRole())) {

                        tasks = new java.util.ArrayList<>();

                        // Tasks created by this manager
                        tasks.addAll(
                                        taskRepository.findByCreatedBy(
                                                        loggedInUser));

                        // Employee-to-Employee tasks
                        tasks.addAll(
                                        taskRepository.findByCreatedBy_RoleAndAssignedTo_Role(
                                                        "EMPLOYEE",
                                                        "EMPLOYEE"));

                        // Remove duplicates
                        tasks = new java.util.ArrayList<>(
                                        tasks.stream()
                                                        .distinct()
                                                        .toList());

                } else {

                        tasks = taskRepository.findByAssignedTo(
                                        loggedInUser);
                }

                tasks.removeIf(task -> "Completed".equalsIgnoreCase(
                                task.getStatus()));

                // ==========================
                // COMPLETED TASKS
                // ==========================

                List<Task> completedTasks;

                if ("MANAGER".equalsIgnoreCase(
                                loggedInUser.getRole())) {

                        completedTasks = new java.util.ArrayList<>();

                        completedTasks.addAll(
                                        taskRepository.findByCreatedBy(
                                                        loggedInUser));

                        completedTasks.addAll(
                                        taskRepository.findByCreatedBy_RoleAndAssignedTo_Role(
                                                        "EMPLOYEE",
                                                        "EMPLOYEE"));

                        completedTasks = new java.util.ArrayList<>(
                                        completedTasks.stream()
                                                        .filter(task -> "Completed".equalsIgnoreCase(
                                                                        task.getStatus()))
                                                        .distinct()
                                                        .toList());

                } else {

                        completedTasks = taskRepository.findByAssignedTo(
                                        loggedInUser);

                        completedTasks = new java.util.ArrayList<>(
                                        completedTasks.stream()
                                                        .filter(task -> "Completed".equalsIgnoreCase(
                                                                        task.getStatus()))
                                                        .distinct()
                                                        .toList());
                }

                // Remove duplicate completed tasks

                Map<String, Task> uniqueCompletedTasks = new LinkedHashMap<>();

                for (Task task : completedTasks) {

                        String key = task.getTitle().trim().toLowerCase()
                                        + "|"
                                        + task.getDescription().trim().toLowerCase();

                        if (!uniqueCompletedTasks.containsKey(key)) {

                                uniqueCompletedTasks.put(
                                                key,
                                                task);
                        }
                }

                completedTasks = List.copyOf(
                                uniqueCompletedTasks.values());

                model.addAttribute(
                                "tasks",
                                tasks);

                model.addAttribute(
                                "completedTasks",
                                completedTasks);

                return "dashboard";
        }
}