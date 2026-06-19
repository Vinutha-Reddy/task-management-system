package com.example.task_management.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.task_management.model.Task;
import com.example.task_management.model.User;
import com.example.task_management.repository.TaskRepository;

@Service
public class TaskNotificationService {

        @Autowired
        private TaskRepository taskRepository;

        @Autowired
        private EmailService emailService;

        @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Kolkata")

        public void runOnStartup() {

                System.out.println("=================================");
                System.out.println("Running task check on application startup...");
                System.out.println("=================================");

                checkTasks();
        }

        public void checkTasks() {

                System.out.println("=================================");
                System.out.println("Scheduler Running...");
                System.out.println("Checking tasks...");
                System.out.println("=================================");

                List<Task> tasks = taskRepository.findAll();

                System.out.println("Total Tasks Found : " + tasks.size());

                for (Task task : tasks) {

                        System.out.println("---------------------------------");
                        System.out.println("Task : " + task.getTitle());

                        User user = task.getAssignedTo();

                        if (user == null) {
                                System.out.println("No assigned user. Skipping.");
                                continue;
                        }

                        LocalDate today = LocalDate.now();
                        LocalDate dueDate = task.getDueDate();

                        if (dueDate == null) {
                                System.out.println("Due date is null. Skipping.");
                                continue;
                        }

                        System.out.println("Today     : " + today);
                        System.out.println("Due Date  : " + dueDate);
                        System.out.println("Status    : " + task.getStatus());

                        long daysLeft = ChronoUnit.DAYS.between(today, dueDate);

                        // =====================================================
                        // REMINDER EMAIL (2 DAYS BEFORE DUE DATE)
                        // =====================================================

                        if (!task.isReminderSent()
                                        && !"Completed".equalsIgnoreCase(task.getStatus())
                                        && daysLeft >= 0
                                        && daysLeft <= 2) {

                                System.out.println(
                                                "Reminder condition matched for task : "
                                                                + task.getTitle());

                                String subject = "Task Deadline Reminder";

                                String body = "Dear " + user.getName() + ",\n\n" +

                                                "This is a reminder regarding an assigned task that is approaching its deadline.\n\n"
                                                +

                                                "Task Details:\n\n" +

                                                "Title: " + task.getTitle() + "\n" +
                                                "Description: " + task.getDescription() + "\n" +
                                                "Due Date: " + task.getDueDate() + "\n\n" +

                                                "Please ensure that the task is completed on or before the due date.\n\n"
                                                +

                                                "Regards,\n" +
                                                "Task Management System";

                                try {

                                        emailService.sendEmail(
                                                        user.getEmail(),
                                                        subject,
                                                        body);

                                        task.setReminderSent(true);
                                        taskRepository.save(task);

                                        System.out.println(
                                                        "Reminder email sent successfully.");

                                } catch (Exception e) {

                                        System.out.println(
                                                        "Failed to send reminder email.");

                                        e.printStackTrace();
                                }
                        }

                        // =====================================================
                        // OVERDUE TASK EMAIL + DELETE
                        // =====================================================

                        if (dueDate.isBefore(today)
                                        && !"Completed".equalsIgnoreCase(task.getStatus())) {

                                System.out.println(
                                                "Overdue task found : "
                                                                + task.getTitle());

                                System.out.println(
                                                "Email will be sent to : "
                                                                + user.getEmail());

                                String subject = "Task Removed Due To Expired Due Date";

                                String body = "Dear " + user.getName() + ",\n\n" +

                                                "This is to inform you that the following task has been removed from the system because its due date has passed and the task was not marked as completed.\n\n"
                                                +

                                                "Task Details:\n\n" +

                                                "Title: " + task.getTitle() + "\n" +
                                                "Description: " + task.getDescription() + "\n" +
                                                "Due Date: " + task.getDueDate() + "\n\n" +

                                                "If this task is still required, please create it again through the Task Management System.\n\n"
                                                +

                                                "Thank you.\n\n" +

                                                "Regards,\n" +
                                                "Task Management System";

                                try {

                                        emailService.sendEmail(
                                                        user.getEmail(),
                                                        subject,
                                                        body);

                                        System.out.println(
                                                        "Overdue task email sent successfully to : "
                                                                        + user.getEmail());

                                        taskRepository.delete(task);

                                        System.out.println(
                                                        "Task deleted successfully : "
                                                                        + task.getTitle());

                                } catch (Exception e) {

                                        System.out.println(
                                                        "Failed to send overdue task email.");

                                        e.printStackTrace();

                                        // Task will NOT be deleted
                                        // if email sending fails
                                }
                        }
                }
        }
}