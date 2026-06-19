package com.example.task_management.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.task_management.model.Comment;
import com.example.task_management.model.Task;
import com.example.task_management.model.User;
import com.example.task_management.repository.CommentRepository;
import com.example.task_management.repository.TaskRepository;
import com.example.task_management.repository.UserRepository;
import com.example.task_management.service.TaskService;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskService taskService; // Inject TaskService

    @GetMapping("/new")
    public String showAddForm(
            Model model,
            @ModelAttribute("deletedTaskTitle") String title,
            @ModelAttribute("deletedTaskDescription") String description,
            @ModelAttribute("deletedTaskPriority") String priority) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User loggedInUser = userRepository.findByEmail(auth.getName());

        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("priority", priority);

        model.addAttribute("userRole",
                loggedInUser.getRole());

        if ("MANAGER".equalsIgnoreCase(loggedInUser.getRole())) {

            model.addAttribute(
                    "employees",
                    userRepository.findByRole("EMPLOYEE"));
        }

        return "task-form";
    }

    @PostMapping("/add")
    public String addTask(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String dueDate,
            @RequestParam String status,
            @RequestParam String priority,
            @RequestParam(required = false) Long assignedUserId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        User loggedInUser = userRepository.findByEmail(email);

        if (loggedInUser == null) {
            return "redirect:/dashboard";
        }

        User assignedUser;

        if ("MANAGER".equalsIgnoreCase(loggedInUser.getRole())
                && assignedUserId != null) {

            assignedUser = userRepository.findById(assignedUserId)
                    .orElse(loggedInUser);

        } else {

            assignedUser = loggedInUser;
        }

        Optional<Task> existingTask = taskRepository.findByTitleIgnoreCaseAndDescriptionIgnoreCase(
                title.trim(),
                description.trim());

        if (existingTask.isPresent()) {

            Task task = existingTask.get();

            task.setDueDate(LocalDate.parse(dueDate));
            task.setStatus(status);
            task.setPriority(priority);

            // Preserve creator
            if (task.getCreatedBy() == null) {
                task.setCreatedBy(loggedInUser);
            }

            task.setAssignedTo(assignedUser);

            taskRepository.save(task);

        } else {

            Task task = new Task();

            task.setTitle(title);
            task.setDescription(description);

            task.setDueDate(LocalDate.parse(dueDate));

            task.setStatus(status);
            task.setPriority(priority);

            // Creator of task
            task.setCreatedBy(loggedInUser);

            // Person responsible for task
            task.setAssignedTo(assignedUser);

            taskRepository.save(task);
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/{id}")
    public String viewTask(@PathVariable Long id, Model model) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        User loggedInUser = userRepository.findByEmail(
                auth.getName());

        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            return "redirect:/dashboard";
        }

        Task task = optionalTask.get();

        boolean allowed = false;

        if ("MANAGER".equalsIgnoreCase(
                loggedInUser.getRole())) {

            // Manager can view tasks created by themselves
            if (task.getCreatedBy() != null
                    && task.getCreatedBy().getId().equals(
                            loggedInUser.getId())) {

                allowed = true;
            }

            // Manager can view Employee -> Employee tasks
            if (task.getCreatedBy() != null
                    && task.getAssignedTo() != null
                    && "EMPLOYEE".equalsIgnoreCase(
                            task.getCreatedBy().getRole())
                    && "EMPLOYEE".equalsIgnoreCase(
                            task.getAssignedTo().getRole())) {

                allowed = true;
            }

        } else {

            // Employee can view only tasks assigned to them
            if (task.getAssignedTo() != null
                    && task.getAssignedTo().getId().equals(
                            loggedInUser.getId())) {

                allowed = true;
            }
        }

        if (!allowed) {
            return "redirect:/dashboard";
        }

        model.addAttribute("task", task);

        model.addAttribute(
                "comments",
                commentRepository.findByTaskIdOrderByTimestampDesc(id));

        return "task-details";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Task not found.");
            return "redirect:/dashboard";
        }

        model.addAttribute("task", task.get());
        return "task-edit";
    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String dueDate,
            @RequestParam String status,
            @RequestParam String priority) {
        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isPresent()) {

            Task task = optionalTask.get();

            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(LocalDate.parse(dueDate));
            task.setStatus(status);
            task.setPriority(priority);

            taskService.updateTask(task);
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskRepository.findById(id).ifPresent(taskRepository::delete);
        redirectAttributes.addFlashAttribute("success", "Task deleted successfully.");
        return "redirect:/dashboard";
    }

    // Updated method to delegate status update & auto-delete to service
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long taskId, @RequestParam String status) {
        taskService.updateTaskStatus(taskId, status);
        return "redirect:/dashboard";
    }

    @PostMapping("/add-comment")
    public String addComment(@RequestParam Long taskId, @RequestParam String content) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        final User user = userRepository.findByEmail(username) != null ? userRepository.findByEmail(username)
                : userRepository.findByUsername(username);

        taskRepository.findById(taskId).ifPresent(task -> {
            Comment comment = new Comment();
            comment.setTask(task);
            comment.setAuthor(user);
            comment.setContent(content);
            comment.setTimestamp(LocalDateTime.now());
            commentRepository.save(comment);
        });

        return "redirect:/tasks/" + taskId;
    }
}
